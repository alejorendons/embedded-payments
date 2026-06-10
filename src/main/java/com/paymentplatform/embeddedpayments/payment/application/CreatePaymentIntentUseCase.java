package com.paymentplatform.embeddedpayments.payment.application;

import com.paymentplatform.embeddedpayments.merchant.domain.entity.Merchant;
import com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantInactiveException;
import com.paymentplatform.embeddedpayments.merchant.domain.exception.MerchantNotFoundException;
import com.paymentplatform.embeddedpayments.merchant.domain.repository.MerchantRepository;
import com.paymentplatform.embeddedpayments.payment.domain.entity.PaymentIntent;
import com.paymentplatform.embeddedpayments.payment.domain.repository.PaymentRepository;
import com.paymentplatform.embeddedpayments.payment.domain.services.PaymentDomainService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentIntentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentDomainService paymentDomainService;
    private final MerchantRepository merchantRepository;

    public CreatePaymentIntentUseCase(PaymentRepository paymentRepository,
                                      PaymentDomainService paymentDomainService,
                                      MerchantRepository merchantRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDomainService = paymentDomainService;
        this.merchantRepository = merchantRepository;
    }

    public PaymentIntent execute(UUID merchantId, BigDecimal amount, String currency) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found: " + merchantId));

        if (!merchant.isActive()) {
            throw new MerchantInactiveException("Merchant is not active and cannot create payment intents");
        }

        PaymentIntent intent = paymentDomainService.createPaymentIntent(merchantId, amount, currency);
        return paymentRepository.save(intent);
    }
}

