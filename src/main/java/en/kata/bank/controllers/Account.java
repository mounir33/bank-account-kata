package en.kata.bank.controllers;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/account")
public class Account {
    private Transaction current = new Transaction(0, 0);
    private final List<Transaction> transactions = new ArrayList<>();

    @PostMapping("/deposit")
    public String depositAmount(@RequestParam int amount) {
        deposit(amount);
        return "Deposit successful. Current balance: " + current.balance();
    }

    @PostMapping("/withdraw")
    public String withdraw1(@RequestParam int amount) {
        return "Withdrawal successful. Current balance: " + deposit(-amount);
    }

    @GetMapping("/statement")
    public String printStatement1() {
        return current.toString();
    }

    @GetMapping("/history")
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactions);
    }

    @GetMapping("/historyByDate")
    public List<Transaction> getTransactionHistoryByDate(@RequestParam String date) {
        LocalDate searchDate = LocalDate.parse(date);
        return transactions.stream()
                .filter(transaction -> transaction.date.toLocalDate().equals(searchDate))
                .collect(Collectors.toList());
    }

    public int deposit(int amount) {
        current = new Transaction(current.balance() + amount, Math.abs(amount));
        transactions.add(current);
        return current.balance();
    }
    private record Transaction(int balance, int amount, LocalDateTime date) {

        public Transaction(int balance, int amount) {
            this(balance, amount, LocalDateTime.now());
        }

        @Override
        public String toString() {
            return String.format("Date Amount Balance%n%s%n%s %d %d",
                    balance < 0 ? "Failed: Insufficient Funds" : amount <= 0 ? "Failed: Negative Transaction" : "",
                    date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), amount, balance);
        }
    }
}



