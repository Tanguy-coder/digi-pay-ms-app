#!/usr/bin/env bash
set -euo pipefail

GW="http://localhost:8888"
SUFFIX=$(date +%s)

echo "=================================================="
echo " Digi-Pay — E2E Gateway Test"
echo " Gateway : $GW"
echo "=================================================="

# ─────────────────────────────────────────────────────
# Setup
# ─────────────────────────────────────────────────────
echo ""
echo ">>> [Setup] Creating customers Alice and Bob..."

ALICE_ID=$(curl -s -X POST "$GW/customer-service/api/v1/customers" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Alice\",
    \"lastName\": \"Gateway\",
    \"email\": \"alice.gw.${SUFFIX}@test.com\",
    \"phoneNumber\": \"+336${SUFFIX}\",
    \"nationality\": \"FRA\",
    \"addressLine1\": \"1 rue de la Paix\",
    \"city\": \"Paris\",
    \"country\": \"France\",
    \"preferredCurrency\": \"EUR\"
  }" | jq -r '.id')

BOB_ID=$(curl -s -X POST "$GW/customer-service/api/v1/customers" \
  -H "Content-Type: application/json" \
  -d "{
    \"firstName\": \"Bob\",
    \"lastName\": \"Gateway\",
    \"email\": \"bob.gw.${SUFFIX}@test.com\",
    \"phoneNumber\": \"+337${SUFFIX}\",
    \"nationality\": \"FRA\",
    \"addressLine1\": \"2 rue de la Paix\",
    \"city\": \"Paris\",
    \"country\": \"France\",
    \"preferredCurrency\": \"EUR\"
  }" | jq -r '.id')

echo "    Alice ID : $ALICE_ID"
echo "    Bob ID   : $BOB_ID"

echo ">>> [Setup] Waiting for Kafka propagation (4s)..."
sleep 4

ALICE_WALLET=$(curl -s "$GW/wallet-service/api/v1/wallets/customer/$ALICE_ID" | jq -r '.id')
BOB_WALLET=$(curl -s "$GW/wallet-service/api/v1/wallets/customer/$BOB_ID" | jq -r '.id')

echo "    Alice Wallet : $ALICE_WALLET"
echo "    Bob Wallet   : $BOB_WALLET"

echo ">>> [Setup] Crediting Alice's wallet with 50 000 EUR..."
curl -s -X POST "$GW/wallet-service/api/v1/wallets/$ALICE_WALLET/credit?amount=50000" > /dev/null

# ─────────────────────────────────────────────────────
# Scenario 1 : paiement normal → CLEARED + COMPLETED
# ─────────────────────────────────────────────────────
echo ""
echo "=================================================="
echo " Scenario 1 : CLEARED — normal payment (100 EUR)"
echo "=================================================="

PAYMENT_1=$(curl -s -X POST "$GW/payment-service/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 100.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-gw-sc1-${SUFFIX}\",
    \"description\": \"Normal payment via gateway\"
  }")

P1_ID=$(echo "$PAYMENT_1" | jq -r '.id')
echo "Payment ID : $P1_ID"
echo "Status     : $(echo "$PAYMENT_1" | jq -r '.status')"

echo ">>> Waiting for fraud analysis (3s)..."
sleep 3

ANALYSIS_1=$(curl -s "$GW/fraud-service/api/v1/fraud-analyses/$P1_ID")
echo "Fraud verdict  : $(echo "$ANALYSIS_1" | jq -r '.verdict')"
echo "Risk score     : $(echo "$ANALYSIS_1" | jq -r '.riskScore')"
echo "Payment status : $(curl -s "$GW/payment-service/api/v1/payments/$P1_ID" | jq -r '.status')"

NOTIFS_1=$(curl -s "$GW/notification-service/api/v1/notifications/payment/$P1_ID")
echo "Notifications  : $(echo "$NOTIFS_1" | jq -r '[.[].type] | join(", ")')"

# ─────────────────────────────────────────────────────
# Scenario 2 : montant eleve → BLOCKED + FAILED
# ─────────────────────────────────────────────────────
echo ""
echo "=================================================="
echo " Scenario 2 : BLOCKED — high amount (15 000 EUR)"
echo "=================================================="

PAYMENT_2=$(curl -s -X POST "$GW/payment-service/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 15000.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-gw-sc2-${SUFFIX}\",
    \"description\": \"High amount payment via gateway\"
  }")

P2_ID=$(echo "$PAYMENT_2" | jq -r '.id')
echo "Payment ID : $P2_ID"

echo ">>> Waiting for fraud analysis (3s)..."
sleep 3

ANALYSIS_2=$(curl -s "$GW/fraud-service/api/v1/fraud-analyses/$P2_ID")
echo "Fraud verdict  : $(echo "$ANALYSIS_2" | jq -r '.verdict')"
echo "Risk score     : $(echo "$ANALYSIS_2" | jq -r '.riskScore')"
echo "Rule triggered : $(echo "$ANALYSIS_2" | jq -r '.rulesTriggered[0].ruleCode')"
echo "Payment status : $(curl -s "$GW/payment-service/api/v1/payments/$P2_ID" | jq -r '.status')"

NOTIFS_2=$(curl -s "$GW/notification-service/api/v1/notifications/payment/$P2_ID")
echo "Notifications  : $(echo "$NOTIFS_2" | jq -r '[.[].type] | join(", ")')"

# ─────────────────────────────────────────────────────
# Scenario 3 : idempotency → 409
# ─────────────────────────────────────────────────────
echo ""
echo "=================================================="
echo " Scenario 3 : Idempotency — replay same key → 409"
echo "=================================================="

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GW/payment-service/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 100.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-gw-sc1-${SUFFIX}\",
    \"description\": \"Duplicate payment\"
  }")
echo "HTTP status : $HTTP_CODE (expected: 409)"

# ─────────────────────────────────────────────────────
# Scenario 4 : historique complet Alice
# ─────────────────────────────────────────────────────
echo ""
echo "=================================================="
echo " Scenario 4 : Alice full history"
echo "=================================================="

echo "--- Payments ---"
curl -s "$GW/payment-service/api/v1/payments/wallet/$ALICE_WALLET" | jq '[.[] | {status, amount}]'

echo "--- Fraud analyses ---"
curl -s "$GW/fraud-service/api/v1/fraud-analyses/customer/$ALICE_WALLET" | jq '[.[] | {verdict, riskScore}]'

echo "--- Notifications ---"
curl -s "$GW/notification-service/api/v1/notifications/wallet/$ALICE_WALLET" | jq '[.[].type]'

echo "--- Wallet balance ---"
curl -s "$GW/wallet-service/api/v1/wallets/$ALICE_WALLET" | jq '{balance, currency}'

echo ""
echo "=================================================="
echo " Done."
echo "=================================================="
