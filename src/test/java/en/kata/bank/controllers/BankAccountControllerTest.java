package en.kata.bank.controllers;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(Account.class)
public class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDepositAmount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "100"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Deposit successful. Current balance: 100"));
    }

    @Test
    public void testWithdraw1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/withdraw")
                        .param("amount", "50"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Withdrawal successful. Current balance: -50"));
    }

    @Test
    public void testPrintStatement1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Your expected statement content"));
    }

    @Test
    public void testGetTransactionHistoryByDate() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = LocalDateTime.now().format(formatter);
        mockMvc.perform(MockMvcRequestBuilders.get("/historyByDate")
                        .param("date", date))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Your expected history content for the given date"));
    }

    @Test
    public void testPrintStatementDisplaysColumnHeadings() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String statement = result.getResponse().getContentAsString();
                    String[] rows = statement.split("\\n");
                    String[] columns = rows[0].split("\\s");
                    String[] expectedColumns = "Date Amount Balance".split(" ");
                    assertArrayEquals(expectedColumns, columns);
                });
    }

    @Test
    public void testDepositAmountIsDisplayedOnStatement() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "500"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String testStatement = result.getResponse().getContentAsString();
                    String[] lines = testStatement.split("\n");
                    StatementLine line = parseStatementLine(lines[lines.length - 1]);
                    assertEquals(500, line.amount());
                });
    }

    @Test
    public void testDepositAmountIsAddedToAccountBalance() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "500"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String testStatement = result.getResponse().getContentAsString();
                    String[] lines = testStatement.split("\n");
                    StatementLine line = parseStatementLine(lines[lines.length - 1]);
                    assertEquals(2300, line.balance());
                });
    }

    @Test
    public void testDepositDateIsDisplayedOnStatement() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "500"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String testStatement = result.getResponse().getContentAsString();
                    String[] lines = testStatement.split("\n");
                    StatementLine line = parseStatementLine(lines[lines.length - 1]);

                    String DATE_FORMATTER = "dd-MM-yyyy";
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
                    assertEquals(LocalDateTime.now().format(formatter), line.date());
                });
    }

    @Test
    public void testWithdrawalAmountIsDisplayedOnStatement() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "500"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/account/withdraw")
                        .param("amount", "100"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String testStatement = result.getResponse().getContentAsString();
                    String[] lines = testStatement.split("\n");
                    StatementLine line = parseStatementLine(lines[lines.length - 1]);
                    assertEquals(100, line.amount());
                });
    }

    @Test
    public void testWithdrawalAmountIsSubtractedFromAccountBalance() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "500"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/account/withdraw")
                        .param("amount", "100"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String testStatement = result.getResponse().getContentAsString();
                    String[] lines = testStatement.split("\n");
                    StatementLine line = parseStatementLine(lines[lines.length - 1]);
                    assertEquals(800, line.balance());
                });
    }

    @Test
    public void testWithdrawalDateIsDisplayedOnStatement() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/account/deposit")
                        .param("amount", "100"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.post("/account/withdraw")
                        .param("amount", "100"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/account/statement"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    String testStatement = result.getResponse().getContentAsString();
                    String[] lines = testStatement.split("\n");
                    StatementLine line = parseStatementLine(lines[lines.length - 1]);

                    String DATE_FORMATTER = "dd-MM-yyyy";
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
                    assertEquals(LocalDateTime.now().format(formatter), line.date());
                });
    }

    @Test
    public void testGetTransactionHistory() throws Exception {
        // Perform a GET request to the /account/history endpoint
        mockMvc.perform(MockMvcRequestBuilders.get("/account/history"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    List<Transaction> transactionHistory = deserializeTransactionList();

                    assertNotNull(transactionHistory);
                    assertEquals(0, transactionHistory.size());
                });
    }


    //test helper method

    private StatementLine parseStatementLine(String statementLine) {
        String[] lineContents = statementLine.trim().split("\\s");
        String date = lineContents[0];
        int amount = Integer.parseInt(lineContents[1]);
        int balance = Integer.parseInt(lineContents[2]);
        return new StatementLine(date, amount, balance);
    }
    private record StatementLine(String date, int amount, int balance){}

    private List<Transaction> deserializeTransactionList() {
        return List.of();
    }

    // Placeholder class for simplicity, replace it with your actual implementation
    private static class Transaction {
        private final  int balance;
        private final int amount;

        public Transaction(int balance, int amount) {
            this.balance = balance;
            this.amount = amount;
        }

        public int getBalance() {
            return balance;
        }

        public int getAmount() {
            return amount;
        }
    }
}