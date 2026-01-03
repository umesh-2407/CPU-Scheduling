import java.util.*;

// This class holds information about one process
class Process {
    
    // Basic information about the process
    int id;                 // Process number (like 1, 2, 3)
    String name;            // Process name (like P1, P2)
    int arrivalTime;        // When process arrives (in seconds)
    int burstTime;          // How long CPU needs to work on it
    int timeLeft;           // How much work is still remaining
    
    // Results after scheduling
    int finishTime;         // When process completes
    int turnaroundTime;     // Total time from arrival to completion
    int waitingTime;        // Time spent waiting for CPU
    
    // Create a new process
    Process(int id, String name, int arrivalTime, int burstTime) {
        this.id = id;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.timeLeft = burstTime;  // Initially all work is remaining
    }
    
    // Calculate turnaround and waiting time
    void calculateTimes() {
        // Turnaround = finish time - arrival time
        turnaroundTime = finishTime - arrivalTime;
        
        // Waiting = turnaround - actual work time
        waitingTime = turnaroundTime - burstTime;
    }
    
    // Print this process's details in a nice format
    void printDetails() {
        System.out.printf("%-5d %-8s %-10d %-10d %-12d %-12d %-10d%n",
                id, name, arrivalTime, burstTime,
                finishTime, turnaroundTime, waitingTime);
    }
}

// Main class that runs all scheduling algorithms
public class CPUScheduler {
    
    // Scanner to read user input
    static Scanner input = new Scanner(System.in);
    
    // List to store all processes
    static ArrayList<Process> processList = new ArrayList<>();
    
    // Main method - program starts here
    public static void main(String[] args) {
        
        // Keep showing menu until user exits
        while (true) {
            
            // Display menu options
            System.out.println("\n===== CPU SCHEDULING SIMULATOR =====");
            System.out.println("1. Load Sample Processes");
            System.out.println("2. View All Processes");
            System.out.println("3. Run FCFS (First Come First Serve)");
            System.out.println("4. Run SJF (Shortest Job First)");
            System.out.println("5. Run Round Robin");
            System.out.println("6. Exit Program");
            System.out.print("Choose an option: ");
            
            int choice = input.nextInt();
            
            // Do action based on user choice
            if (choice == 1) {
                loadSampleProcesses();
            } else if (choice == 2) {
                showAllProcesses();
            } else if (choice == 3) {
                runFCFS();
            } else if (choice == 4) {
                runSJF();
            } else if (choice == 5) {
                runRoundRobin();
            } else if (choice == 6) {
                System.out.println("Goodbye!");
                break;  // Exit the program
            } else {
                System.out.println("Invalid choice! Try again.");
            }
        }
    }
    
    // Load some example processes into the list
    static void loadSampleProcesses() {
        processList.clear();  // Remove old processes
        
        // Add 4 sample processes
        processList.add(new Process(1, "P1", 0, 5));
        processList.add(new Process(2, "P2", 1, 3));
        processList.add(new Process(3, "P3", 2, 8));
        processList.add(new Process(4, "P4", 3, 6));
        
        System.out.println("✓ Loaded 4 sample processes!");
    }
    
    // Show all processes in a table
    static void showAllProcesses() {
        
        // Check if list is empty
        if (processList.isEmpty()) {
            System.out.println("No processes available. Load sample data first!");
            return;
        }
        
        // Print table header
        System.out.printf("%-5s %-8s %-10s %-10s%n",
                "ID", "Name", "Arrival", "Burst");
        
        // Print each process
        for (Process p : processList) {
            System.out.printf("%-5d %-8s %-10d %-10d%n",
                    p.id, p.name, p.arrivalTime, p.burstTime);
        }
    }
    
    // ALGORITHM 1: First Come First Serve
    // Execute processes in the order they arrive
    static void runFCFS() {
        
        // Make a copy so we don't change original data
        ArrayList<Process> workList = copyAllProcesses();
        
        // Sort by arrival time (earliest first)
        workList.sort((p1, p2) -> p1.arrivalTime - p2.arrivalTime);
        
        int currentTime = 0;  // CPU clock starts at 0
        
        // Process each job one by one
        for (Process p : workList) {
            
            // If process hasn't arrived yet, CPU waits
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            
            // CPU works on this process
            currentTime = currentTime + p.burstTime;
            
            // Record when it finished
            p.finishTime = currentTime;
            p.calculateTimes();
        }
        
        // Show the results
        showResults("FCFS (First Come First Serve)", workList);
    }
    
    // ALGORITHM 2: Shortest Job First (Non-Preemptive)
    // Always pick the shortest job that has arrived
    static void runSJF() {
        
        ArrayList<Process> workList = copyAllProcesses();
        int totalProcesses = workList.size();
        
        // Track which processes are done
        boolean[] isDone = new boolean[totalProcesses];
        int completedCount = 0;
        int currentTime = 0;
        
        // Keep going until all processes are done
        while (completedCount < totalProcesses) {
            
            int shortestIndex = -1;      // Index of shortest job
            int shortestBurst = 999999;  // Very large number
            
            // Look through all processes
            for (int i = 0; i < totalProcesses; i++) {
                Process p = workList.get(i);
                
                // Check if this process is:
                // 1. Not done yet
                // 2. Has arrived
                // 3. Has shorter burst time than current shortest
                if (!isDone[i] && p.arrivalTime <= currentTime && p.burstTime < shortestBurst) {
                    shortestBurst = p.burstTime;
                    shortestIndex = i;
                }
            }
            
            // If no process is ready, CPU is idle (move time forward)
            if (shortestIndex == -1) {
                currentTime++;
            } else {
                // Execute the shortest job
                Process p = workList.get(shortestIndex);
                currentTime = currentTime + p.burstTime;
                p.finishTime = currentTime;
                p.calculateTimes();
                
                // Mark as completed
                isDone[shortestIndex] = true;
                completedCount++;
            }
        }
        
        showResults("SJF (Shortest Job First)", workList);
    }
    
    // ALGORITHM 3: Round Robin
    static void runRoundRobin() {

    // Ask user for time quantum
    System.out.print("Enter time quantum: ");
    int timeQuantum = input.nextInt();

    // Make a copy of processes so original data is safe
    ArrayList<Process> workList = copyAllProcesses();

    // Sort processes by arrival time
    workList.sort((p1, p2) -> p1.arrivalTime - p2.arrivalTime);

    // Queue to store ready processes
    Queue<Process> readyQueue = new LinkedList<>();

    int currentTime = 0;     // CPU clock
    int finishedCount = 0;   // Number of completed processes
    int index = 0;           // Points to next arriving process

    // Run until all processes are finished
    while (finishedCount < workList.size()) {

        // Step 1: Add all processes that have arrived to the queue
        while (index < workList.size() &&
               workList.get(index).arrivalTime <= currentTime) {

            readyQueue.add(workList.get(index));
            index++;
        }

        // Step 2: If no process is ready, CPU is idle
        if (readyQueue.isEmpty()) {
            currentTime++;
            continue;
        }

        // Step 3: Take first process from queue
        Process currentProcess = readyQueue.poll();

        // Step 4: Execute for time quantum or remaining time
        int executeTime = Math.min(timeQuantum, currentProcess.timeLeft);
        currentProcess.timeLeft -= executeTime;
        currentTime += executeTime;

        // Step 5: Check if new processes arrived during execution
        while (index < workList.size() &&
               workList.get(index).arrivalTime <= currentTime) {

            readyQueue.add(workList.get(index));
            index++;
        }

        // Step 6: If process still has work, put it back in queue
        if (currentProcess.timeLeft > 0) {
            readyQueue.add(currentProcess);
        }
        // Step 7: If process finished, record times
        else {
            currentProcess.finishTime = currentTime;
            currentProcess.calculateTimes();
            finishedCount++;
        }
    }

    // Show final result
    showResults("ROUND ROBIN (Quantum = " + timeQuantum + ")", workList);
}

    
    // Make a copy of all processes (so we don't modify originals)
    static ArrayList<Process> copyAllProcesses() {
        ArrayList<Process> newList = new ArrayList<>();
        
        for (Process p : processList) {
            newList.add(new Process(p.id, p.name, p.arrivalTime, p.burstTime));
        }
        
        return newList;
    }
    
    // Display scheduling results in a nice table
    static void showResults(String algorithmName, ArrayList<Process> resultList) {
        
        System.out.println("\n========== " + algorithmName + " ==========");
        
        // Print header
        System.out.printf("%-5s %-8s %-10s %-10s %-12s %-12s %-10s%n",
                "ID", "Name", "Arrival", "Burst",
                "Finish", "Turnaround", "Waiting");
        
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        
        // Print each process result
        for (Process p : resultList) {
            p.printDetails();
            totalWaitingTime = totalWaitingTime + p.waitingTime;
            totalTurnaroundTime = totalTurnaroundTime + p.turnaroundTime;
        }
        
        // Calculate and show averages
        int numberOfProcesses = resultList.size();
        double avgWaiting = totalWaitingTime / numberOfProcesses;
        double avgTurnaround = totalTurnaroundTime / numberOfProcesses;
        
        System.out.println("\n--- Summary ---");
        System.out.printf("Average Waiting Time: %.2f%n", avgWaiting);
        System.out.printf("Average Turnaround Time: %.2f%n", avgTurnaround);
    }
}
