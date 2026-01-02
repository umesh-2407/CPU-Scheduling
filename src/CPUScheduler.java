import java.util.*;

/* =========================================================
   PROCESS CLASS
   Stores all details related to a process
   ========================================================= */
class Process {

    int pid;                // Process ID
    String name;            // Process Name
    int arrivalTime;        // Arrival Time
    int burstTime;          // Burst Time (CPU required)
    int remainingTime;      // Remaining time (for Round Robin)

    int completionTime;     // Completion Time
    int turnaroundTime;     // Turnaround Time
    int waitingTime;        // Waiting Time

    // Constructor
    Process(int pid, String name, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    // Calculate Turnaround & Waiting Time
    void calculateTimes() {
        turnaroundTime = completionTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;
    }

    // Display process details in table format
    void display() {
        System.out.printf(
                "%-5d %-8s %-10d %-10d %-12d %-12d %-10d%n",
                pid, name, arrivalTime, burstTime,
                completionTime, turnaroundTime, waitingTime
        );
    }
}

/* =========================================================
   CPU SCHEDULER CLASS
   Implements FCFS, SJF, and Round Robin
   ========================================================= */
public class CPUScheduler {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Process> processes = new ArrayList<>();

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n=========== CPU SCHEDULING SIMULATOR ===========");
            System.out.println("1. Load Sample Data");
            System.out.println("2. View Processes");
            System.out.println("3. FCFS Scheduling");
            System.out.println("4. SJF Scheduling");
            System.out.println("5. Round Robin Scheduling");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> loadSampleData();
                case 2 -> viewProcesses();
                case 3 -> fcfs();
                case 4 -> sjf();
                case 5 -> roundRobin();
                case 6 -> {
                    System.out.println("Thank you!");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    /* ================= SAMPLE DATA ================= */
    static void loadSampleData() {
        processes.clear();
        processes.add(new Process(1, "P1", 0, 5));
        processes.add(new Process(2, "P2", 1, 3));
        processes.add(new Process(3, "P3", 2, 8));
        processes.add(new Process(4, "P4", 3, 6));
        System.out.println("✓ Sample data loaded");
    }

    /* ================= VIEW PROCESSES ================= */
    static void viewProcesses() {
        if (processes.isEmpty()) {
            System.out.println("No processes available!");
            return;
        }

        System.out.printf("%-5s %-8s %-10s %-10s%n",
                "PID", "Name", "Arrival", "Burst");

        for (Process p : processes) {
            System.out.printf("%-5d %-8s %-10d %-10d%n",
                    p.pid, p.name, p.arrivalTime, p.burstTime);
        }
    }

    /* ================= FCFS =================
       First Come First Serve
       Processes are executed in order of arrival
       ======================================= */
    static void fcfs() {
        ArrayList<Process> temp = copyProcesses();

        // Sort by arrival time
        temp.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0;

        for (Process p : temp) {
            // CPU waits if process arrives later
            time = Math.max(time, p.arrivalTime);

            // Execute process
            time += p.burstTime;

            p.completionTime = time;
            p.calculateTimes();
        }

        displayResult("FCFS", temp);
    }

    /* ================= SJF (Non-Preemptive) =================
       Shortest Job First
       Process with smallest burst time executes first
       ======================================================= */
    static void sjf() {
        ArrayList<Process> temp = copyProcesses();
        int n = temp.size();

        boolean[] completed = new boolean[n];
        int done = 0;
        int time = 0;

        while (done < n) {
            int index = -1;
            int minBurst = Integer.MAX_VALUE;

            // Find shortest available job
            for (int i = 0; i < n; i++) {
                Process p = temp.get(i);
                if (!completed[i] && p.arrivalTime <= time && p.burstTime < minBurst) {
                    minBurst = p.burstTime;
                    index = i;
                }
            }

            // If no process is ready, CPU is idle
            if (index == -1) {
                time++;
            } else {
                Process p = temp.get(index);
                time += p.burstTime;
                p.completionTime = time;
                p.calculateTimes();
                completed[index] = true;
                done++;
            }
        }

        displayResult("SJF", temp);
    }

    /* ================= ROUND ROBIN =================
       Each process gets fixed time quantum
       ================================================= */
    static void roundRobin() {
        System.out.print("Enter Time Quantum: ");
        int quantum = sc.nextInt();

        ArrayList<Process> temp = copyProcesses();
        temp.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Queue<Process> queue = new LinkedList<>();
        int time = temp.get(0).arrivalTime;
        int finished = 0;

        queue.add(temp.get(0));

        while (finished < temp.size()) {

            if (queue.isEmpty()) {
                time++;
                continue;
            }

            Process p = queue.poll();

            // Execute process
            int exec = Math.min(quantum, p.remainingTime);
            p.remainingTime -= exec;
            time += exec;

            // Add newly arrived processes
            for (Process pr : temp) {
                if (pr.arrivalTime <= time && pr.remainingTime > 0 && !queue.contains(pr)) {
                    queue.add(pr);
                }
            }

            // If process still has work left
            if (p.remainingTime > 0) {
                queue.add(p);
            } else {
                p.completionTime = time;
                p.calculateTimes();
                finished++;
            }
        }

        displayResult("ROUND ROBIN", temp);
    }

    /* ================= UTIL METHODS ================= */
    static ArrayList<Process> copyProcesses() {
        ArrayList<Process> list = new ArrayList<>();
        for (Process p : processes) {
            list.add(new Process(p.pid, p.name, p.arrivalTime, p.burstTime));
        }
        return list;
    }

    static void displayResult(String algo, ArrayList<Process> list) {
        System.out.println("\n========== " + algo + " ==========");
        System.out.printf(
                "%-5s %-8s %-10s %-10s %-12s %-12s %-10s%n",
                "PID", "Name", "Arrival", "Burst",
                "Completion", "Turnaround", "Waiting"
        );

        double totalWT = 0, totalTAT = 0;

        for (Process p : list) {
            p.display();
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.printf("\nAverage Waiting Time: %.2f%n", totalWT / list.size());
        System.out.printf("Average Turnaround Time: %.2f%n", totalTAT / list.size());
    }
}
