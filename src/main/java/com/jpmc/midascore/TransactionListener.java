package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRecordRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TransactionListener {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;
    private final RestTemplate restTemplate;

    public TransactionListener(UserRepository userRepository,
                               TransactionRecordRepository transactionRepository,
                               RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-group"
    )
    public void listen(Transaction transaction) {

        // 🔹 Fetch users
        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        // 🔹 Validation
        if (sender == null || recipient == null) return;
        if (sender.getBalance() < transaction.getAmount()) return;

        // 🔹 Call Incentive API
        Incentive incentiveResponse = restTemplate.postForObject(
                "http://localhost:8080/incentive",
                transaction,
                Incentive.class
        );

        float incentive = (incentiveResponse != null) ? incentiveResponse.getAmount() : 0f;

        // 🔹 Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());

        recipient.setBalance(
                recipient.getBalance() + transaction.getAmount() + incentive
        );

        System.out.println("Sender: " + sender.getName());
        System.out.println("Recipient: " + recipient.getName());
        System.out.println("Amount: " + transaction.getAmount());
        System.out.println("Recipient balance: " + recipient.getBalance());

        // 🔹 Save users
        userRepository.save(sender);
        userRepository.save(recipient);

        // 🔹 Save transaction WITH incentive
        TransactionRecord record = new TransactionRecord(
                sender,
                recipient,
                transaction.getAmount(),
                incentive
        );

        transactionRepository.save(record);

        // 🔹 Debug print for Wilbur
        if (sender.getName().equals("wilbur") || recipient.getName().equals("wilbur")) {
            System.out.println("Wilbur balance: " +
                    (sender.getName().equals("wilbur") ? sender.getBalance() : recipient.getBalance()));
        }
    }
}