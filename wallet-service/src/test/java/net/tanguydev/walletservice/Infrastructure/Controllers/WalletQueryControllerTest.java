package net.tanguydev.walletservice.Infrastructure.Controllers;

import net.tanguydev.walletservice.Domain.Entities.DomainWallet;
import net.tanguydev.walletservice.Domain.Enums.WalletStatus;
import net.tanguydev.walletservice.Domain.Enums.WalletType;
import net.tanguydev.walletservice.Domain.Presenters.WalletPresenterInterface;
import net.tanguydev.walletservice.Domain.Responses.WalletResponse;
import net.tanguydev.walletservice.Domain.UseCases.FindWalletByCustomerIdUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.FindWalletByIdUseCaseInterface;
import net.tanguydev.walletservice.Domain.UseCases.GetWalletHistoryUseCaseInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletQueryController.class)
class WalletQueryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private FindWalletByIdUseCaseInterface findById;
    @MockitoBean private FindWalletByCustomerIdUseCaseInterface findByCustomerId;
    @MockitoBean private GetWalletHistoryUseCaseInterface getHistory;
    @MockitoBean private WalletPresenterInterface presenter;

    private static final UUID WALLET_ID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID OTHER_ID    = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Test
    void show_shouldReturn200() throws Exception {
        DomainWallet domain = buildWallet();
        WalletResponse response = buildResponse();

        when(findById.execute(WALLET_ID)).thenReturn(Optional.of(domain));
        when(presenter.present(any(DomainWallet.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/" + WALLET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(10000));
    }

    @Test
    void show_shouldReturn404_whenNotFound() throws Exception {
        when(findById.execute(OTHER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wallets/" + OTHER_ID))
                .andExpect(status().isNotFound());
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
