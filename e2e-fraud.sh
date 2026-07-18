#!/usr/bin/env bash
set -euo pipefail

CUSTOMER_URL="http://localhost:8082/api/v1/customers"
WALLET_URL="http://localhost:8083/api/v1/wallets"
PAYMENT_URL="http://localhost:8084/api/v1/payments"
FRAUD_URL="http://localhost:8085/api/v1/fraud-analyses"

echo "=================================================="
echo " Digi-Pay — E2E Fraud Detection Scenarios"
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
    \"lastName\": \"Fraud\",
    \"email\": \"alice.fraud.${SUFFIX}@test.com\",
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
    \"lastName\": \"Fraud\",
    \"email\": \"bob.fraud.${SUFFIX}@test.com\",
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

echo ">>> [Setup] Crediting Alice's wallet with 50 000..."
curl -s -X POST "$WALLET_URL/$ALICE_WALLET/credit?amount=50000" > /dev/null

echo ""
echo "=================================================="
echo " Scenario 1 : CLEARED — normal payment (100 EUR)"
echo "=================================================="

PAYMENT_1=$(curl -s -X POST "$PAYMENT_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 100.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-fraud-sc1-$(date +%s)\",
    \"description\": \"Normal payment\"
  }")

PAYMENT_1_ID=$(echo "$PAYMENT_1" | jq -r '.id')
echo "Payment ID : $PAYMENT_1_ID"
echo "Status     : $(echo "$PAYMENT_1" | jq -r '.status')"

echo ">>> Waiting for fraud analysis (2s)..."
sleep 2

ANALYSIS_1=$(curl -s "$FRAUD_URL/$PAYMENT_1_ID")
echo "Fraud verdict  : $(echo "$ANALYSIS_1" | jq -r '.verdict')"
echo "Risk score     : $(echo "$ANALYSIS_1" | jq -r '.riskScore')"
echo "Rules triggered: $(echo "$ANALYSIS_1" | jq -r '.rulesTriggered | length')"

echo ""
echo "=================================================="
echo " Scenario 2 : BLOCKED — high amount (15 000 EUR)"
echo "=================================================="

PAYMENT_2=$(curl -s -X POST "$PAYMENT_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 15000.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-fraud-sc2-$(date +%s)\",
    \"description\": \"High amount payment\"
  }")

PAYMENT_2_ID=$(echo "$PAYMENT_2" | jq -r '.id')
echo "Payment ID : $PAYMENT_2_ID"
echo "Status     : $(echo "$PAYMENT_2" | jq -r '.status')"

echo ">>> Waiting for fraud analysis (2s)..."
sleep 2

ANALYSIS_2=$(curl -s "$FRAUD_URL/$PAYMENT_2_ID")
echo "Fraud verdict  : $(echo "$ANALYSIS_2" | jq -r '.verdict')"
echo "Risk score     : $(echo "$ANALYSIS_2" | jq -r '.riskScore')"
echo "Rule triggered : $(echo "$ANALYSIS_2" | jq -r '.rulesTriggered[0].ruleCode')"

echo ""
echo "=================================================="
echo " Scenario 3 : REVIEW — velocity (4 rapid payments)"
echo "=================================================="

echo ">>> Sending 4 payments in rapid succession..."
for i in 1 2 3 4; do
  curl -s -X POST "$PAYMENT_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"senderWalletId\": \"$ALICE_WALLET\",
      \"receiverWalletId\": \"$BOB_WALLET\",
      \"amount\": 50.00,
      \"currency\": \"EUR\",
      \"type\": \"P2P\",
      \"idempotencyKey\": \"e2e-fraud-sc3-${i}-$(date +%s%N)\",
      \"description\": \"Velocity test $i\"
    }" > /dev/null
  echo "    Payment $i sent"
done

PAYMENT_VEL=$(curl -s -X POST "$PAYMENT_URL" \
  -H "Content-Type: application/json" \
  -d "{
    \"senderWalletId\": \"$ALICE_WALLET\",
    \"receiverWalletId\": \"$BOB_WALLET\",
    \"amount\": 50.00,
    \"currency\": \"EUR\",
    \"type\": \"P2P\",
    \"idempotencyKey\": \"e2e-fraud-sc3-5-$(date +%s%N)\",
    \"description\": \"Velocity test 5 — should trigger\"
  }")

PAYMENT_VEL_ID=$(echo "$PAYMENT_VEL" | jq -r '.id')
echo "Payment ID : $PAYMENT_VEL_ID"

echo ">>> Waiting for fraud analysis (2s)..."
sleep 2

ANALYSIS_VEL=$(curl -s "$FRAUD_URL/$PAYMENT_VEL_ID")
echo "Fraud verdict  : $(echo "$ANALYSIS_VEL" | jq -r '.verdict')"
echo "Risk score     : $(echo "$ANALYSIS_VEL" | jq -r '.riskScore')"
echo "Rules triggered: $(echo "$ANALYSIS_VEL" | jq -r '[.rulesTriggered[].ruleCode] | join(", ")')"

echo ""
echo "=================================================="
echo " Scenario 4 : customer fraud history"
echo "=================================================="

echo ">>> Fetching all fraud analyses for Alice (by walletId — used as customerId workaround)..."
HISTORY=$(curl -s "$FRAUD_URL/customer/$ALICE_WALLET")
echo "Total analyses : $(echo "$HISTORY" | jq '. | if type == "array" then length else 0 end')"
echo "Verdicts       : $(echo "$HISTORY" | jq -r 'if type == "array" then [.[].verdict] | join(", ") else "N/A" end')"

echo ""
echo "=================================================="
echo " Done."
echo "=================================================="
