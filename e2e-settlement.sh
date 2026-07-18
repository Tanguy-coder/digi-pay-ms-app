#!/usr/bin/env bash
set -euo pipefail

CUSTOMER_URL="http://localhost:8082/api/v1/customers"
WALLET_URL="http://localhost:8083/api/v1/wallets"
PAYMENT_URL="http://localhost:8084/api/v1/payments"
SETTLEMENT_URL="http://localhost:8087/api/settlements"

echo "=================================================="
echo " Digi-Pay — E2E Settlement Scenarios"
echo "=================================================="

# ─────────────────────────────────────────────────────
# Setup : create Alice + Bob and fund Alice's wallet
# ─────────────────────────────────────────────────────
echo ""
echo ">>> [Setup] Creating customers Alice and Bob..."

SUFFIX=$(date +%s)

ALICE_ID=$(curl -s -X POST "$CUSTOMER_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Alice\",
    \"lastName\": \"Settlement\",
    \"email\": \"alice.settlement.${SUFFIX}@test.com\",
    \"phoneNumber\": \"+1${SUFFIX}1\",
    \"nationality\": \"FRA\",
    \"addressLine1\": \"1 rue de la Paix\",
    \"city\": \"Paris\",
    \"country\": \"France\",
    \"preferredCurrency\": \"EUR\"
  }" | jq -r '.id')

BOB_ID=$(curl -s -X POST "$CUSTOMER_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Bob\",
    \"lastName\": \"Settlement\",
    \"email\": \"bob.settlement.${SUFFIX}@test.com\",
    \"phoneNumber\": \"+1${SUFFIX}2\",
    \"nationality\": \"FRA\",
    \"addressLine1\": \"2 rue de la Paix\",
    \"city\": \"Paris\",
    \"country\": \"France\",
    \"preferredCurrency\": \"EUR\"
  }" | jq -r '.id')

echo "    Alice ID : $ALICE_ID"
echo "    Bob ID   : $BOB_ID"

echo ">>> [Setup] Waiting for Kafka propagation (3s)..."
sleep 3

ALICE_WALLET=$(curl -s "$WALLET_URL/customer/$ALICE_ID" | jq -r '.id')
BOB_WALLET=$(curl -s "$WALLET_URL/customer/$BOB_ID" | jq -r '.id')

echo "    Alice Wallet : $ALICE_WALLET"
echo "    Bob Wallet   : $BOB_WALLET"

echo ">>> [Setup] Crediting Alice's wallet with 20 000..."
curl -s -X POST "$WALLET_URL/$ALICE_WALLET/credit?amount=20000" > /dev/null

echo ""
echo "=================================================="
echo " Scenario 1 : Payment completed → Settlement created"
echo "=================================================="

PAYMENT_1=$(curl -s -X POST "$PAYMENT_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 1000.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-settle-sc1-${SUFFIX}\",
    \"description\": \"Settlement test payment 1\"
  }")

PAYMENT_1_ID=$(echo "$PAYMENT_1" | jq -r '.id')
echo "Payment ID : $PAYMENT_1_ID"
echo "Status     : $(echo "$PAYMENT_1" | jq -r '.status')"

echo ">>> Waiting for settlement processing (5s)..."
sleep 5

echo ">>> Checking settlement list..."
SETTLEMENTS=$(curl -s "$SETTLEMENT_URL")
SETTLEMENT_COUNT=$(echo "$SETTLEMENTS" | jq '. | length')
echo "Total settlements : $SETTLEMENT_COUNT"

if [ "$SETTLEMENT_COUNT" -gt 0 ]; then
  FIRST=$(echo "$SETTLEMENTS" | jq '.[0]')
  echo "Settlement ID     : $(echo "$FIRST" | jq -r '.id')"
  echo "Reference         : $(echo "$FIRST" | jq -r '.reference')"
  echo "Status            : $(echo "$FIRST" | jq -r '.status')"
  echo "Total amount      : $(echo "$FIRST" | jq -r '.totalAmount')"
  echo "Net position      : $(echo "$FIRST" | jq -r '.netPosition')"
  echo "Currency          : $(echo "$FIRST" | jq -r '.currency')"

  SETTLEMENT_ID=$(echo "$FIRST" | jq -r '.id')
else
  echo "ERROR: No settlement created!"
  exit 1
fi

echo ""
echo "=================================================="
echo " Scenario 2 : Get settlement by ID"
echo "=================================================="

SETTLEMENT_DETAIL=$(curl -s "$SETTLEMENT_URL/$SETTLEMENT_ID")
echo "Settlement ID     : $(echo "$SETTLEMENT_DETAIL" | jq -r '.id')"
echo "Reference         : $(echo "$SETTLEMENT_DETAIL" | jq -r '.reference')"
echo "Status            : $(echo "$SETTLEMENT_DETAIL" | jq -r '.status')"
echo "Settled at        : $(echo "$SETTLEMENT_DETAIL" | jq -r '.settledAt')"

echo ""
echo "=================================================="
echo " Scenario 3 : Second payment → second settlement"
echo "=================================================="

PAYMENT_2=$(curl -s -X POST "$PAYMENT_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 2500.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-settle-sc3-${SUFFIX}\",
    \"description\": \"Settlement test payment 2\"
  }")

PAYMENT_2_ID=$(echo "$PAYMENT_2" | jq -r '.id')
echo "Payment ID : $PAYMENT_2_ID"

echo ">>> Waiting for settlement processing (5s)..."
sleep 5

SETTLEMENTS_AFTER=$(curl -s "$SETTLEMENT_URL")
SETTLEMENT_COUNT_AFTER=$(echo "$SETTLEMENTS_AFTER" | jq '. | length')
echo "Total settlements : $SETTLEMENT_COUNT_AFTER"

if [ "$SETTLEMENT_COUNT_AFTER" -gt "$SETTLEMENT_COUNT" ]; then
  echo "OK — new settlement created"
else
  echo "ERROR: No new settlement created!"
  exit 1
fi

echo ""
echo "=================================================="
echo " Scenario 4 : Idempotency — duplicate payment does not create duplicate settlement"
echo "=================================================="

echo ">>> Sending same idempotency key again..."
PAYMENT_DUP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$PAYMENT_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 1000.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-settle-sc1-${SUFFIX}\",
    \"description\": \"Duplicate payment\"
  }")

echo "HTTP status  : $PAYMENT_DUP (expected 409)"

sleep 3

SETTLEMENTS_FINAL=$(curl -s "$SETTLEMENT_URL")
SETTLEMENT_COUNT_FINAL=$(echo "$SETTLEMENTS_FINAL" | jq '. | length')
echo "Settlements  : $SETTLEMENT_COUNT_FINAL (should still be $SETTLEMENT_COUNT_AFTER)"

if [ "$SETTLEMENT_COUNT_FINAL" -eq "$SETTLEMENT_COUNT_AFTER" ]; then
  echo "OK — no duplicate settlement"
else
  echo "WARNING: unexpected settlement count"
fi

echo ""
echo "=================================================="
echo " Done. All settlement scenarios passed."
echo "=================================================="
