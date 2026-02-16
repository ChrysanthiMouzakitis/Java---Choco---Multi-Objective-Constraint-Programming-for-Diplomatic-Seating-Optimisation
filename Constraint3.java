
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.io.IOException;

//this file satifies part (iii) of the assignment
//bag apcking global constrint was not used here
//as not all country delegate have to be place in a car
//therefore, this file uses a different method to the previous two

public class Assignment1_Part3 {
    public static void main(String[] args) throws IOException {

        //read in inputs
        CouncilData data = new CouncilData("C:\\Users\\35387\\Documents\\CS4626\\src\\main\\resources\\council3.txt");
        int numCountries = data.getnCountries();
        int numCars = data.getnCars();
        int numSeats = data.getnSeats();

        int [] donations = data.getdonations();
        int [][] pairs = data.getpairs();

        // Create a Model
        Model model = new Model("the first CA");

        // Create variables****************************************

        //totalPeople is an array that records for each car, the total number of delegates
        IntVar[] totalPeople = model.intVarArray("total people", numCars, 0, numSeats);

        //a 2D array of 0/1, so that carPacking[i][j]==1 means that delegate j was allocated car i
        IntVar[][] carPacking = model.intVarMatrix("solution", numCars, numCountries, 0 , 1);

        //sum all donations to create an upper bound for all donations
        int upperBoundForDonations = 0;
        for (int d : donations){
            upperBoundForDonations += d;
        }

        //for each car, add up the total donations from all people in that car
        IntVar[] donationsOfInvited = model.intVarArray("donations of invited", numCars, 0, upperBoundForDonations);

        //record all donations across all cars (will be maxmised later)
        IntVar totalDonations = model.intVar("total donations", 0, upperBoundForDonations);


        //Constraints********************************

        //for each car: add the sizes of its objects to the get the cars total donations
        for (int car = 0; car<numCars; car++) {
            model.scalar(carPacking[car], donations, "=", donationsOfInvited[car]).post();
        }

        //add all total donations from the donations of invited array
        model.sum(donationsOfInvited, "=", totalDonations).post();


       //for each car, count how many people are in it
        for (int car = 0; car<numCars; car++) {
            model.sum(carPacking[car], "=", totalPeople[car]).post();
        }

        //for each delegate, make it so that it is in 0 or 1 cars, not 2+
        for (int delegate = 0; delegate < numCountries; delegate++) {
            model.sum(ArrayUtils.getColumn(carPacking, delegate), "<=", 1).post();
        }

        //ensure no unfriendly countries together
        //ensure their 1 in the carpacking matrix is not in the same row

        for (int i = 0; i < numCountries; i++) {
            for (int j = i+1; j < numCountries; j++) {
                if(pairs[i][j]==1){
                    //for every car, the sum of those countries columns for that car less than or equal to 1
                    //meaning they are never both in the same car
                    for (int car = 0; car < numCars; car++) {
                        model.arithm(carPacking[car][i], "+", carPacking[car][j], "<=", 1).post();
                    }
                }

            }
        }

        //symmetry breaker constraint
        //for each car, make sure that its carLoad is not larger than the next car
        for (int car = 0; car < numCars-1; car++) {
            model.arithm(totalPeople[car], "<=", totalPeople[car+1]).post();
        }


        //SOLVE********************************
        Solver solver = model.getSolver();
        //maxmise total donations
        model.setObjective(Model.MAXIMIZE, totalDonations);

        //Java variables used for displaying output
        int leastcarsUsed = numCars;
        int carsUsed;

        //        if (solver.solve()) {
        while (solver.solve()) { //print the solution
            System.out.println("Solution " + solver.getSolutionCount() + ":");


            carsUsed = 0;
            for (int car = 0; car < numCars; car++) {

                System.out.print("car " + car + ": ");
                for (int object = 0; object < numCountries; object++) {
                    if (carPacking[car][object].getValue() == 1) {
                        System.out.print(object + "(" + donations[object] + ") ");
                    }
                }
                System.out.println("[" + totalPeople[car].getValue() + "]");
                if (totalPeople[car].getValue() > 0) {
                    carsUsed++;
                }

            }
            System.out.println("total donations: "+ totalDonations.getValue());
            leastcarsUsed = Math.min(leastcarsUsed, carsUsed);
            /**/
        }
        System.out.println("Fewest cars used: " + leastcarsUsed);
        solver.printStatistics();
    }
}