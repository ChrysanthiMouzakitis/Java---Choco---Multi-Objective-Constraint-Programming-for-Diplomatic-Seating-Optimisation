
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.io.IOException;

//this file staifies part (ii) of the assignment

public class Assignment1_Part2 {
    public static void main(String[] args) throws IOException {

        //read in inputs
        CouncilData data = new CouncilData("C:\\Users\\35387\\Documents\\CS4626\\src\\main\\resources\\council1.txt");
        int numCountries = data.getnCountries();

        //changed numcars to be the same as the number of countries
        //as that is the maximum number of cars that could be used, an upper bound
        int numCars = numCountries;
        int numSeats = data.getnSeats();

        int [] donations = data.getdonations();
        int [][] pairs = data.getpairs();


        Model model = new Model("the first CA");

        // Create variables**********************************

        //which Car is an array which records for each country, the car the delegate should travel in
        IntVar[] whichCar = model.intVarArray("cars", numCountries, 0, numCars-1);

        //totalPeople is an array that records for each car, the total number of delegates
        IntVar[] totalPeople = model.intVarArray("total people", numCars, 0, numSeats);

        //I created an array sizes with all 1's for the binpacking global constraint
        //it is populated with all 1's as all delegates are (assumed) the same size
        int[] sizes = new int[numCountries];

        for (int i = 0; i < numCountries; i++){
            sizes[i] = 1;
        }

        //record the total number of cars used (will be minimised later)
        IntVar totalCarsUsed = model.intVar("totalCarsUsed", 0, numCars);

        //Constraints*************************************

        //a global bin packing constraint
        model.binPacking(whichCar, sizes, totalPeople,0).post();

        //ensure no unfriendly countries together
        for (int i = 0; i < numCountries; i++) {
            for (int j = i+1; j < numCountries; j++) {
                if(pairs[i][j]==1){
                    model.arithm(whichCar[i], "!=", whichCar[j]).post();
                }

            }
        }

        //symmetry breaker constraint
        //for each bin, make sure that its binLoad is not larger than the next bin
        for (int bin = 0; bin < numCars-1; bin++) {
            model.arithm(totalPeople[bin], "<=", totalPeople[bin+1]).post();
        }

        //the number of different **values** in the IntVarArray whichCar is the same as total cars used
        model.nValues(whichCar,totalCarsUsed).post();


        //SOLVE*************************************
        Solver solver = model.getSolver();
        //minimise total cars
        model.setObjective(Model.MINIMIZE, totalCarsUsed);

        while (solver.solve()) {
            System.out.println("Solution " + solver.getSolutionCount() + ":");


            for (int car = 0; car < numCars; car++) {
                System.out.print("car " + car + ": ");
                for (int object = 0; object < numCountries; object++) {
                    if (whichCar[object].getValue() == car) {
                        System.out.print(object + " ");
                    }
                }
                System.out.println("[" + totalPeople[car].getValue() + "]");

            }
            System.out.println("Fewest cars used: " + totalCarsUsed.getValue());
        }

        solver.printStatistics();
    }
}