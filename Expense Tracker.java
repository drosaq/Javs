
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

/**
 * Smart Expense Tracker - A complete CLI application for managing expenses
 */
public class ExpenseTracker {
    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("💰 SMART EXPENSE TRACKER - COMMAND LINE");
        System.out.println("=".repeat(60));
        
        boolean running = true;
        while (running) {
            displayMenu();
            System.out.print("\nEnter choice (1-9): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1 -> addExpense(manager, scanner);
                    case 2 -> viewAllExpenses(manager);
                    case 3 -> viewByCategory(manager);
                    case 4 -> searchExpenses(manager, scanner);
                    case 5 -> deleteExpense(manager, scanner);
                    case 6 -> showStatistics(manager);
                    case 7 -> exportToCSV(manager, scanner);
                    case 8 -> showHelp();
                    case 9 -> {
                        System.out.println("\n💾 Saving data... Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("❌ Invalid choice! Please enter 1-9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number!");
            }
        }
        scanner.close();
    }
    
    private static void displayMenu() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("📋 MAIN MENU");
        System.out.println("─".repeat(50));
        System.out.println("1. ➕ Add New Expense");
        System.out.println("2. 📄 View All Expenses");
        System.out.println("3. 📊 View by Category");
        System.out.println("4. 🔍 Search Expenses");
        System.out.println("5. 🗑️  Delete Expense");
        System.out.println("6. 📈 Show Statistics");
        System.out.println("7. 📤 Export to CSV");
        System.out.println("8. ❓ Help");
        System.out.println("9. 🚪 Exit");
    }
    
    private static void addExpense(ExpenseManager manager, Scanner scanner) {
        System.out.println("\n➕ ADD NEW EXPENSE");
        System.out.println("-".repeat(30));
        
        try {
            System.out.print("Description: ");
            String description = scanner.nextLine().trim();
            
            if (description.isEmpty()) {
                System.out.println("❌ Description cannot be empty!");
                return;
            }
            
            System.out.print("Amount: $");
            double amount;
            try {
                amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("❌ Amount must be positive!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number!");
                return;
            }
            
            System.out.println("Categories: Food(1) Transport(2) Shopping(3) Entertainment(4) Bills(5) Other(6)");
            System.out.print("Choose category (1-6): ");
            int catChoice = Integer.parseInt(scanner.nextLine());
            
            String category = switch (catChoice) {
                case 1 -> "Food";
                case 2 -> "Transport";
                case 3 -> "Shopping";
                case 4 -> "Entertainment";
                case 5 -> "Bills";
                case 6 -> "Other";
                default -> "Other";
            };
            
            Expense expense = new Expense(description, amount, category);
            manager.addExpense(expense);
            
            System.out.println("✅ Expense added successfully!");
            System.out.printf("📝 Added: %s | $%.2f | %s%n", 
                expense.getDescription(), expense.getAmount(), expense.getCategory());
            
        } catch (Exception e) {
            System.out.println("❌ Error adding expense: " + e.getMessage());
        }
    }
    
    private static void viewAllExpenses(ExpenseManager manager) {
        System.out.println("\n📄 ALL EXPENSES");
        System.out.println("=".repeat(80));
        
        List<Expense> expenses = manager.getAllExpenses();
        
        if (expenses.isEmpty()) {
            System.out.println("No expenses found. Add some expenses first!");
            return;
        }
        
        System.out.printf("%-8s %-25s %-10s %-15s %-20s%n", 
            "ID", "Description", "Amount", "Category", "Date");
        System.out.println("-".repeat(80));
        
        double total = 0;
        for (Expense expense : expenses) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            System.out.printf("%-8s %-25s $%-9.2f %-15s %-20s%n",
                expense.getId().substring(0, 6),
                truncate(expense.getDescription(), 23),
                expense.getAmount(),
                expense.getCategory(),
                sdf.format(expense.getDate()));
            total += expense.getAmount();
        }
        
        System.out.println("-".repeat(80));
        System.out.printf("💵 TOTAL: $%.2f | 📊 Count: %d%n", total, expenses.size());
    }
    
    private static void viewByCategory(ExpenseManager manager) {
        System.out.println("\n📊 EXPENSES BY CATEGORY");
        System.out.println("=".repeat(50));
        
        Map<String, Double> categoryTotals = manager.getCategoryTotals();
        Map<String, List<Expense>> byCategory = manager.getExpensesByCategory();
        
        if (categoryTotals.isEmpty()) {
            System.out.println("No expenses found. Add some expenses first!");
            return;
        }
        
        double overallTotal = manager.getTotalExpenses();
        
        // Display summary
        System.out.println("\n📈 CATEGORY SUMMARY:");
        System.out.println("-".repeat(50));
        
        categoryTotals.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> {
                double percentage = (entry.getValue() / overallTotal) * 100;
                String bar = generateBar(percentage, 20);
                System.out.printf("%-15s: $%-10.2f %5.1f%% %s%n",
                    entry.getKey(), entry.getValue(), percentage, bar);
            });
        
        System.out.println("-".repeat(50));
        System.out.printf("💵 OVERALL TOTAL: $%.2f%n", overallTotal);
        
        // View details for a specific category
        Scanner tempScanner = new Scanner(System.in);
        System.out.print("\nEnter category to view details (or press Enter to skip): ");
        String category = tempScanner.nextLine();
        
        if (!category.isEmpty() && byCategory.containsKey(category)) {
            System.out.println("\n📝 Expenses in " + category + ":");
            System.out.println("-".repeat(60));
            
            byCategory.get(category).forEach(expense -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                System.out.printf("  %-25s $%-8.2f %s%n",
                    truncate(expense.getDescription(), 23),
                    expense.getAmount(),
                    sdf.format(expense.getDate()));
            });
        }
    }
    
    private static void searchExpenses(ExpenseManager manager, Scanner scanner) {
        System.out.print("\n🔍 Enter search keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        
        if (keyword.isEmpty()) {
            System.out.println("❌ Please enter a search keyword!");
            return;
        }
        
        List<Expense> results = manager.searchExpenses(keyword);
        
        System.out.println("\n🔎 SEARCH RESULTS for: \"" + keyword + "\"");
        System.out.println("=".repeat(60));
        
        if (results.isEmpty()) {
            System.out.println("No expenses found matching your search.");
            return;
        }
        
        double total = 0;
        for (Expense expense : results) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            System.out.printf("  %-25s $%-8.2f %-12s %s%n",
                truncate(expense.getDescription(), 23),
                expense.getAmount(),
                expense.getCategory(),
                sdf.format(expense.getDate()));
            total += expense.getAmount();
        }
        
        System.out.println("-".repeat(60));
        System.out.printf("📊 Found: %d expenses | 💵 Total: $%.2f%n", results.size(), total);
    }
    
    private static void deleteExpense(ExpenseManager manager, Scanner scanner) {
        viewAllExpenses(manager);
        List<Expense> expenses = manager.getAllExpenses();
        
        if (expenses.isEmpty()) {
            return;
        }
        
        System.out.print("\n🗑️  Enter expense ID to delete (first 6 chars): ");
        String id = scanner.nextLine().trim();
        
        if (id.length() < 6) {
            System.out.println("❌ Please enter at least 6 characters of the ID!");
            return;
        }
        
        Expense toDelete = null;
        for (Expense expense : expenses) {
            if (expense.getId().startsWith(id)) {
                toDelete = expense;
                break;
            }
        }
        
        if (toDelete != null) {
            System.out.printf("\n⚠️  Confirm delete: %s | $%.2f | %s%n",
                toDelete.getDescription(), toDelete.getAmount(), toDelete.getCategory());
            System.out.print("Type 'yes' to confirm: ");
            
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                if (manager.deleteExpense(toDelete.getId())) {
                    System.out.println("✅ Expense deleted successfully!");
                } else {
                    System.out.println("❌ Failed to delete expense!");
                }
            } else {
                System.out.println("❌ Deletion cancelled.");
            }
        } else {
            System.out.println("❌ No expense found with that ID!");
        }
    }
    
    private static void showStatistics(ExpenseManager manager) {
        System.out.println("\n📈 EXPENSE STATISTICS");
        System.out.println("=".repeat(60));
        
        List<Expense> expenses = manager.getAllExpenses();
        
        if (expenses.isEmpty()) {
            System.out.println("No expenses to analyze. Add some expenses first!");
            return;
        }
        
        // Basic statistics
        double total = manager.getTotalExpenses();
        int count = expenses.size();
        double average = total / count;
        
        // Find min and max
        Expense minExpense = expenses.stream()
            .min(Comparator.comparingDouble(Expense::getAmount))
            .orElse(null);
        Expense maxExpense = expenses.stream()
            .max(Comparator.comparingDouble(Expense::getAmount))
            .orElse(null);
        
        // Recent expenses (last 7 days)
        Calendar weekAgo = Calendar.getInstance();
        weekAgo.add(Calendar.DAY_OF_YEAR, -7);
        long recentCount = expenses.stream()
            .filter(e -> e.getDate().after(weekAgo.getTime()))
            .count();
        
        System.out.printf("📊 Total Expenses: $%.2f%n", total);
        System.out.printf("📈 Number of Expenses: %d%n", count);
        System.out.printf("📉 Average per Expense: $%.2f%n", average);
        System.out.printf("🆕 Recent (7 days): %d expenses%n", recentCount);
        
        if (minExpense != null && maxExpense != null) {
            System.out.printf("📉 Smallest: %s ($%.2f)%n", 
                truncate(minExpense.getDescription(), 20), minExpense.getAmount());
            System.out.printf("📈 Largest: %s ($%.2f)%n", 
                truncate(maxExpense.getDescription(), 20), maxExpense.getAmount());
        }
        
        // Monthly breakdown
        System.out.println("\n📅 MONTHLY BREAKDOWN:");
        System.out.println("-".repeat(40));
        
        Map<String, Double> monthly = manager.getMonthlyTotals();
        monthly.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                System.out.printf("  %-15s: $%-10.2f%n", entry.getKey(), entry.getValue());
            });
        
        // Category insights
        System.out.println("\n🏷️  CATEGORY INSIGHTS:");
        System.out.println("-".repeat(40));
        
        Map<String, Double> categories = manager.getCategoryTotals();
        categories.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                double percentage = (entry.getValue() / total) * 100;
                System.out.printf("  %-15s: $%-8.2f (%5.1f%%)%n",
                    entry.getKey(), entry.getValue(), percentage);
            });
    }
    
    private static void exportToCSV(ExpenseManager manager, Scanner scanner) {
        System.out.print("\n📤 Enter filename for export (e.g., expenses.csv): ");
        String filename = scanner.nextLine().trim();
        
        if (filename.isEmpty()) {
            filename = "expenses_" + System.currentTimeMillis() + ".csv";
        }
        
        if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }
        
        try {
            int count = manager.exportToCSV(filename);
            System.out.printf("✅ Exported %d expenses to: %s%n", count, filename);
            System.out.println("📁 File saved in: " + new File(filename).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("❌ Error exporting: " + e.getMessage());
        }
    }
    
    private static void showHelp() {
        System.out.println("\n❓ HELP & TIPS");
        System.out.println("=".repeat(60));
        System.out.println("""
            📋 EXPENSE TRACKER COMMANDS:
            
            1. ADD EXPENSE: Record your daily expenses
               - Enter clear descriptions for easy searching
               - Categorize expenses for better analysis
            
            2. VIEW ALL: See all your expenses in a neat table
               - Shows ID, description, amount, category, and date
               - IDs are auto-generated for reference
            
            3. VIEW BY CATEGORY: Analyze spending by category
               - Visual bar chart shows spending distribution
               - View details for specific categories
            
            4. SEARCH: Find expenses by keyword
               - Search in descriptions and categories
               - Case-insensitive matching
            
            5. DELETE: Remove unwanted expenses
               - Use the short ID (first 6 characters)
               - Confirm with 'yes' to prevent accidents
            
            6. STATISTICS: Get insights into your spending
               - Monthly breakdowns
               - Category insights
               - Recent activity
            
            7. EXPORT: Save data to CSV file
               - Open in Excel or Google Sheets
               - Use for tax purposes or budgeting
            
            💡 TIPS:
            • Be consistent with descriptions
            • Review your spending weekly
            • Export data regularly for backup
            """);
    }
    
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private static String generateBar(double percentage, int length) {
        int bars = (int) (percentage / (100.0 / length));
        return "[" + "█".repeat(bars) + " ".repeat(length - bars) + "]";
    }
}

/**
 * Expense Model Class
 */
class Expense implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String description;
    private final double amount;
    private final String category;
    private final Date date;
    
    public Expense(String description, double amount, String category) {
        this.id = UUID.randomUUID().toString();
        this.description = description.trim();
        this.amount = amount;
        this.category = category;
        this.date = new Date();
    }
    
    // Getters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return String.format("%s | %s | $%.2f | %s | %s", 
            id.substring(0, 8), description, amount, category, sdf.format(date));
    }
}

/**
 * Expense Manager - Handles business logic
 */
class ExpenseManager {
    private static final String DATA_FILE = "expenses.dat";
    private List<Expense> expenses;
    
    public ExpenseManager() {
        expenses = loadExpenses();
    }
    
    public void addExpense(Expense expense) {
        expenses.add(expense);
        saveExpenses();
    }
    
    public boolean deleteExpense(String id) {
        boolean removed = expenses.removeIf(e -> e.getId().equals(id));
        if (removed) {
            saveExpenses();
        }
        return removed;
    }
    
    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }
    
    public List<Expense> searchExpenses(String keyword) {
        return expenses.stream()
            .filter(e -> e.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                        e.getCategory().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public double getTotalExpenses() {
        return expenses.stream()
            .mapToDouble(Expense::getAmount)
            .sum();
    }
    
    public Map<String, Double> getCategoryTotals() {
        Map<String, Double> totals = new HashMap<>();
        for (Expense expense : expenses) {
            totals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }
        return totals;
    }
    
    public Map<String, List<Expense>> getExpensesByCategory() {
        Map<String, List<Expense>> map = new HashMap<>();
        for (Expense expense : expenses) {
            map.computeIfAbsent(expense.getCategory(), k -> new ArrayList<>())
               .add(expense);
        }
        return map;
    }
    
    public Map<String, Double> getMonthlyTotals() {
        Map<String, Double> monthly = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        
        for (Expense expense : expenses) {
            String month = sdf.format(expense.getDate());
            monthly.merge(month, expense.getAmount(), Double::sum);
        }
        return monthly;
    }
    
    public int exportToCSV(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("ID,Description,Amount,Category,Date");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Expense expense : expenses) {
                writer.printf("%s,\"%s\",%.2f,%s,%s%n",
                    expense.getId(),
                    expense.getDescription().replace("\"", "\"\""),
                    expense.getAmount(),
                    expense.getCategory(),
                    sdf.format(expense.getDate()));
            }
            return expenses.size();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Expense> loadExpenses() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Expense>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("⚠️  Could not load saved expenses. Starting fresh.");
            return new ArrayList<>();
        }
    }
    
    private void saveExpenses() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(expenses);
        } catch (IOException e) {
            System.out.println("⚠️  Could not save expenses: " + e.getMessage());
        }
    }
}