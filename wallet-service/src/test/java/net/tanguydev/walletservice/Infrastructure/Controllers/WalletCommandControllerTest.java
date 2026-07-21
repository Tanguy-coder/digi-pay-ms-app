package net.tanguydev.walletservice.Infrastructure.Controllers;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Presenters.WalletPresenterInterface;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Domain.UseCases.*;
import net.tanguydev.walletservice.Infrastructure.Mappers.WalletMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletCommandController.class)
class WalletCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CreateWalletUseCaseInterface create;
    @MockitoBean private CreditWalletUseCaseInterface credit;
    @MockitoBean private DebitWalletUseCaseInterface debit;
    @MockitoBean private FreezeAmountUseCaseInterface freeze;
    @MockitoBean private WalletPresenterInterface presenter;
    @MockitoBean private WalletMapper mapper;

    private static final UUID WALLET_ID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");

    @Test
    void store_shouldReturn201() throws Exception {
        DomainWallet domain = buildWallet();
        WalletResponse response = buildResponse();

        when(mapper.requestToDomain(any())).thenReturn(domain);
        when(create.execute(any(DomainWallet.class))).thenReturn(domain);
        when(presenter.present(any(DomainWallet.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"" + CUSTOMER_ID + "\", \"walletType\": \"PERSONAL\", \"currency\": \"XOF\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("XOF"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void store_shouldReturn422_whenCurrencyMissing() throws Exception {
        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"" + CUSTOMER_ID + "\", \"walletType\": \"PERSONAL\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void credit_shouldReturn200() throws Exception {
        DomainWallet domain = buildWallet();
        domain.setBalance(new BigDecimal("15000"));
        WalletResponse response = buildResponse();
        response.setBalance(new BigDecimal("15000"));

        when(credit.execute(WALLET_ID, new BigDecimal("5000"))).thenReturn(domain);
        when(presenter.present(any(DomainWallet.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/" + WALLET_ID + "/credit?amount=5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(15000));
    }

    @Test
    void debit_shouldReturn200() throws Exception {
        DomainWallet domain = buildWallet();
        domain.setBalance(new BigDecimal("7000"));
        WalletResponse response = buildResponse();
        response.setBalance(new BigDecimal("7000"));

        when(debit.execute(WALLET_ID, new BigDecimal("3000"))).thenReturn(domain);
        when(presenter.present(any(DomainWallet.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/" + WALLET_ID + "/debit?amount=3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(7000));
    }

    @Test
    void freeze_shouldReturn200() throws Exception {
        DomainWallet domain = buildWallet();
        domain.setFrozenAmount(new BigDecimal("4000"));
        WalletResponse response = buildResponse();
        response.setFrozenAmount(new BigDecimal("4000"));

        when(freeze.execute(WALLET_ID, new BigDecimal("4000"))).thenReturn(domain);
        when(presenter.present(any(DomainWallet.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wallets/" + WALLET_ID + "/freeze?amount=4000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frozenAmount").value(4000));
    }

    private DomainWallet buildWallet() {
        DomainWallet w = new DomainWallet();
        w.setId(WALLET_ID);
        w.setCustomerId(CUSTOMER_ID);
        w.setWalletType(WalletType.PERSONAL);
        w.setWalletNumber("WLT-0000000001");
        w.setCurrency("XOF");
        w.setBalance(new BigDecimal("10000"));
        w.setFrozenAmount(BigDecimal.ZERO);
        w.setStatus(WalletStatus.ACTIVE);
        return w;
    }

    private WalletResponse buildResponse() {
        WalletResponse r = new WalletResponse();
        r.setId(WALLET_ID);
        r.setCustomerId(CUSTOMER_ID);
        r.setWalletType(WalletType.PERSONAL);
        r.setWalletNumber("WLT-0000000001");
        r.setCurrency("XOF");
        r.setBalance(new BigDecimal("10000"));
        r.setFrozenAmount(BigDecimal.ZERO);
        r.setAvailableBalance(new BigDecimal("10000"));
        r.setStatus(WalletStatus.ACTIVE);
        return r;
    }
}
