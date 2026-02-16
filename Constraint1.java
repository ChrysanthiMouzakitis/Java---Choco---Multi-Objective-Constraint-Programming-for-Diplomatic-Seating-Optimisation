
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import java.io.IOException;

//this file satisfies part (i) of the assignment


public class Assignment1 {
    public static void main(String[] args) throws IOException {

        //read in inputs
        //FIXME Change here for different filename to read in the file
        CouncilData data = new CouncilData("C:\\Users\\35387\\Documents\\CS4626\\src\\main\\resources\\council0.txt");
        int numCountries = data.getnCountries();
        int numCars = data.getnCars();
        int numSeats = data.getnSeats();

        int[] donations = data.getdonations();
        int[][] pairs = data.getpairs();

        // Create a Model
        Model model = new Model("the first CA");

        // Create variables*************************************

        //which Car is an array which records for each country, the car the delegate should travel in
        IntVar[] whichCar = model.intVarArray("cars", numCountries, 0, numCars - 1);

        //totalPeople is an array that records for each car, the total number of delegates
        IntVar[] totalPeople = model.intVarArray("total people", numCars, 0, numSeats);

        //I created an array sizes with all 1's for the binpacking global constraint
        //it is populated with all 1's as all delegates are the same size
        int[] sizes = new int[numCountries];
        for (int i = 0; i < numCountries; i++){
            sizes[i] = 1;
        }


        //CONSTRAINTS*******************************

        //the global bin packing constraint
        model.binPacking(whichCar, sizes, totalPeople, 0).post();

        //ensure no unfriendly countries together
        for (int i = 0; i < numCountries; i++) {
            //check all pairs of countries starting from i+1 to ensure no duplicates checked
            for (int j = i + 1; j < numCountries; j++) {
                if (pairs[i][j] == 1) {
                    model.arithm(whichCar[i], "!=", whichCar[j]).post();
                }

            }
        }

        //symmetry breaker constraint
        //for each bin, make sure that its binLoad is not larger than the next bin
        for (int bin = 0; bin < numCars - 1; bin++) {
            model.arithm(totalPeople[bin], "<=", totalPeople[bin + 1]).post();
        }


        //SOLVE*************************************
        Solver solver = model.getSolver();

        //Java variables used for displaying output
        int leastCarsUsed = numCars;
        int carsUsed;

        if (!solver.solve()) {

            System.out.println("No solution found, cannot seat all country delegates within number of cars provided");

        } else {
            do {
                System.out.println("Solution " + solver.getSolutionCount() + ":");

                carsUsed = 0;
                for (int car = 0; car < numCars; car++) {
                    System.out.print("car " + car + ": ");
                    for (int object = 0; object < numCountries; object++) {
                        if (whichCar[object].getValue() == car) {
                            System.out.print(object + " ");
                        }
                    }
                    System.out.println("[" + totalPeople[car].getValue() + "]");
                    if (totalPeople[car].getValue() > 0) {
                        carsUsed++;
                    }
                }
                leastCarsUsed = Math.min(leastCarsUsed, carsUsed);
            } while (solver.solve());

            System.out.println("Fewest cars used: " + leastCarsUsed);
            solver.printStatistics();


        }
    }
}
