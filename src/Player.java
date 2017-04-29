import java.text.DecimalFormat;
import java.util.*;


/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.
        Point[] land = new Point[surfaceN];

        for (int i = 0; i < surfaceN; i++) {
            land[i] = new Point(in.nextInt(), in.nextInt());
        }
        Surface surface = new Surface(land,surfaceN);
        surface.build();
        System.err.println(Utils.toString(surface));

        final Shuttle marsLander = new Shuttle();
        int turnNb = 0;
        int timeout = 100;
        GA genetic = new GA(marsLander, surface);

        // game loop
        while (true) {
            // Update the shuttle status;
            if ( turnNb > 0 ) { // recalculer les vrai valeurs !!! (non arrondies)
                in.nextInt();in.nextInt();in.nextInt();in.nextInt();in.nextInt();in.nextInt();in.nextInt();
                genetic.simulateOneTurnOutcome(marsLander, genetic.toPlay.rotate, genetic.toPlay.power);
                System.err.println(Utils.toString(marsLander));
            } else {
                marsLander.pos.x = in.nextInt();
                marsLander.pos.y = in.nextInt();
                marsLander.hSpeed = in.nextInt(); // the horizontal speed (in m/s), can be negative.
                marsLander.vSpeed = in.nextInt(); // the vertical speed (in m/s), can be negative.
                marsLander.fuel = in.nextInt(); // the quantity of remaining fuel in liters.
                marsLander.rotate = in.nextInt(); // the rotation angle in degrees (-90 to 90).
                marsLander.power = in.nextInt(); // the thrust power (0 to 4).
                marsLander.status = Status.FLYING;
            }

            genetic.run(turnNb,timeout);

            System.out.println(genetic.toPlay.rotate+" "+genetic.toPlay.power); // 2 integers: rotate power. rotate is the desired rotation angle (should be 0 for level 1), power is the desired thrust power (0 to 4).
            turnNb++;
        }
    }

    static class GA {


        static final int MAX_ITERATION_NB = 10000;
        static final int POPULATION_SIZE = 30;
        static final int INDIVIDUAL_SIZE = 100;
        static final int SELECTED_NB = POPULATION_SIZE / 2;
        static final int MUTATION_PERCENTAGE = 6;
        static final int ELITE_REINJECT_NB = 3;

        int turnNb;                     // current turn number.
        int iteration;                  // current iteration number.
        int individualEffectiveSize;    // the number of genes to read for the current turnNb.

        Control[][] currentPopulation;
        int[] fitness;          // fitness[i] gives the fitness of individual i (int the CURRENT population).
        final Shuttle[] finalShuttleCurPop;

        Control[][] nextPopulation;

        final Control toPlay;
        final Control[] elite;

        final Shuttle realShuttle;    // the real shuttle.
        final Surface surface;

        GA(final Shuttle realShuttle, final Surface surface) {
            this.turnNb = 0;
            this.currentPopulation = new Control[POPULATION_SIZE][INDIVIDUAL_SIZE];
            this.nextPopulation = new Control[POPULATION_SIZE][INDIVIDUAL_SIZE];
            this.fitness = new int[POPULATION_SIZE];
            this.finalShuttleCurPop = new Shuttle[POPULATION_SIZE];
            for (int i=0; i<POPULATION_SIZE; i++) {
                this.finalShuttleCurPop[i] = new Shuttle();
                for (int j=0; j<INDIVIDUAL_SIZE; j++) {
                    this.currentPopulation[i][j] = new Control();
                    this.nextPopulation[i][j] = new Control();
                }
            }
            this.realShuttle = realShuttle;
            this.surface = surface;
            this.individualEffectiveSize = INDIVIDUAL_SIZE;
            this.elite = new Control[INDIVIDUAL_SIZE];
            for (int i=0; i<INDIVIDUAL_SIZE; i++) {
                this.elite[i] = new Control();
            }
            this.toPlay = new Control();
        }

        void run(int turnNb, int timeout) {
            long start = System.nanoTime();
            long duration, end;
            this.iteration = 0;
            this.turnNb = turnNb;
            this.individualEffectiveSize = INDIVIDUAL_SIZE ;

            // 1 - Generate a random population
            this.generateInitialPopulation();
            this.reinjectTheBestIndividual();
            do {

                // 2 - Evaluate each individual of the current population.
                this.simulateFinalOutcome();
                this.computeFitness();

                // 3 - Select the toPlay individuals.
                this.selectionByTournament();

                // 4 - Crossover and mutation.
                this.crossover2Points();
                this.mutation();

                this.prepareNextIteration();
                end = System.nanoTime();
                duration = (end - start) / 1000000;
            } while (this.iteration<MAX_ITERATION_NB && duration < timeout);
            // ----- Final (partial) iteration -----
            this.simulateFinalOutcome();
            this.computeFitness();
            this.findTheBestAction();
            // -------------------------------------
            System.err.println("#iterations="+this.iteration+" in "+duration+" ms");
        }

        void prepareNextIteration() {
            this.iteration++;
            Control[][] swap = this.currentPopulation;
            this.currentPopulation = this.nextPopulation;
            this.nextPopulation = swap;
        }

        void simulateFinalOutcome() {
            for (int indIdx=0; indIdx<POPULATION_SIZE; indIdx++) {
                final Shuttle shuttle = this.finalShuttleCurPop[indIdx];
                shuttle.copyContentOf(this.realShuttle);
                int geneIdx = 0;
                while (shuttle.status == Status.FLYING && geneIdx < this.individualEffectiveSize) {
                    this.simulateOneTurnOutcome(shuttle, this.currentPopulation[indIdx][geneIdx].rotate, this.currentPopulation[indIdx][geneIdx].power);
                    geneIdx++;
                }
            }
        }

        void simulateOneTurnOutcome(final Shuttle shuttle, int rotate, int power) {
            //System.err.println(">>> simulateOneTurnOutcome : rotate="+rotate+", power="+power);
            // 1 - Compute control
            if ( power > shuttle.power ) {
                shuttle.power++;
            } else if ( power < shuttle.power ) {
                shuttle.power--;
            }
            if ( rotate > shuttle.rotate ) {
                shuttle.rotate = Utils.min(rotate, shuttle.rotate+15);
            } else if ( rotate < shuttle.rotate ) {
                shuttle.rotate = Utils.max(rotate, shuttle.rotate-15);
            }
            // 2 - Compute remaining fuel
            shuttle.fuel -= shuttle.power;
            // 3 - Compute new position
            Point prevPos = new Point(shuttle.pos.x, shuttle.pos.y);
            shuttle.pos.x = shuttle.pos.x + shuttle.hSpeed - 0.5 * (Math.sin(Math.toRadians(shuttle.rotate)) * shuttle.power);
            shuttle.pos.y = shuttle.pos.y + shuttle.vSpeed + 0.5 * (Math.cos(Math.toRadians(shuttle.rotate)) * shuttle.power - 3.711 );
            // 4 - Compute new speed
            shuttle.hSpeed = shuttle.hSpeed - Math.sin(Math.toRadians(shuttle.rotate)) * shuttle.power;
            shuttle.vSpeed = shuttle.vSpeed + Math.cos(Math.toRadians(shuttle.rotate)) * shuttle.power - 3.7111;

            // 5 - Compute new status
            if (!surface.collideWithGround(prevPos, shuttle.pos)) {
                shuttle.status = Status.FLYING;
            } else {
                if (Utils.abs(shuttle.hSpeed) <= 20 && Utils.abs(shuttle.vSpeed) <= 40 && shuttle.rotate == 0 &&
                        surface.collideWithFlatZone(prevPos, shuttle.pos) ) {
                    shuttle.status = Status.LANDED;
                } else {
                    shuttle.status = Status.CRASHED;
                }
            }
            //System.err.println("<<< simulateOneTurnOutcome : "+Utils.toString(shuttle));
        }

        void computeFitness() {

            Shuttle s;
            int fit;
            for (int i=0; i<POPULATION_SIZE; i++) {
                s = this.finalShuttleCurPop[i];
                if (s.status == Status.LANDED) {
                    fit = 10 * s.fuel;
                } else if (s.status == Status.FLYING) {
                    fit = (int) -surface.horizontalDistanceFromFlatZone(s.pos);
                    if ( !surface.collideWithGround(s.pos, new Point(surface.flatMidX, surface.flatY+1)) ) fit = 50;
                } else {
                    fit = (int) (Utils.min(-Utils.abs(s.vSpeed)+40,0)
                            +Utils.min(-Utils.abs(s.hSpeed)+20,0)
                            +(-Utils.abs(s.rotate))
                            -surface.horizontalDistanceFromFlatZone(s.pos));
                }
                this.fitness[i] = fit;
            }

        }

        // ---------------------------------
        // ---------- SELECTIONS ----------
        // ---------------------------------
        void selectionByTournament() {
            int ind1, ind2, winner;
            int selectedIndNb = 0;
            for (int indIdx=0; indIdx<SELECTED_NB; indIdx++) {
                ind1 = Utils.rnd.nextInt(POPULATION_SIZE);
                ind2 = Utils.rnd.nextInt(POPULATION_SIZE);
                if ( this.fitness[ind1] > this.fitness[ind2] ) {
                    winner = ind1;
                } else {
                    winner = ind2;
                }
                Utils.copyContent(this.currentPopulation[winner], this.nextPopulation[selectedIndNb], this.individualEffectiveSize);
                selectedIndNb++;
            }
            // Elitism ! (override 1st prev selected)
            int bestInd = 0;
            int bestFit = this.fitness[0];
            for (int i=1; i<POPULATION_SIZE; i++) {
                if ( this.fitness[i] > bestFit ) {
                    bestFit = this.fitness[i];
                    bestInd = i;
                }
            }
            //System.err.print(";"+bestFit);
            Utils.copyContent(this.currentPopulation[bestInd], this.nextPopulation[0], this.individualEffectiveSize);
        }


        // ---------------------------------
        // ---------- CROSSOVER ----------
        // ---------------------------------
        // 1 pivot
        void crossover1Point() {
            int ind1, ind2, pivot;
            for (int child=SELECTED_NB; child<POPULATION_SIZE; child++) {
                ind1 = Utils.rnd.nextInt(SELECTED_NB);
                ind2 = Utils.rnd.nextInt(SELECTED_NB);
                pivot = Utils.rnd.nextInt(this.individualEffectiveSize);
                for (int gene=0; gene<this.individualEffectiveSize; gene++) {
                    if (gene<pivot) {
                        this.nextPopulation[child][gene].rotate = this.nextPopulation[ind1][gene].rotate;
                        this.nextPopulation[child][gene].power = this.nextPopulation[ind1][gene].power;
                    } else {
                        this.nextPopulation[child][gene].rotate = this.nextPopulation[ind2][gene].rotate;
                        this.nextPopulation[child][gene].power = this.nextPopulation[ind2][gene].power;
                    }
                }
            }
        }

        // 2 pivots
        void crossover2Points() {
            int ind1, ind2, pivot1, pivot2;
            for (int child=SELECTED_NB; child<POPULATION_SIZE; child++) {
                ind1 = Utils.rnd.nextInt(SELECTED_NB);
                ind2 = Utils.rnd.nextInt(SELECTED_NB);
                pivot1 = Utils.rnd.nextInt(this.individualEffectiveSize);
                pivot2 = Utils.rnd.nextInt(this.individualEffectiveSize);
                for (int gene=0; gene<this.individualEffectiveSize; gene++) {
                    if (gene<Utils.min(pivot1,pivot2) || gene>Utils.max(pivot1,pivot2)) {
                        this.nextPopulation[child][gene].rotate = this.nextPopulation[ind1][gene].rotate;
                        this.nextPopulation[child][gene].power = this.nextPopulation[ind1][gene].power;
                    } else {
                        this.nextPopulation[child][gene].rotate = this.nextPopulation[ind2][gene].rotate;
                        this.nextPopulation[child][gene].power = this.nextPopulation[ind2][gene].power;
                    }
                }
            }
        }

        // uniform
        void crossoverUniform() {
            int ind1, ind2;
            for (int child=SELECTED_NB; child<POPULATION_SIZE; child++) {
                ind1 = Utils.rnd.nextInt(SELECTED_NB);
                ind2 = Utils.rnd.nextInt(SELECTED_NB);
                for (int gene=0; gene<this.individualEffectiveSize; gene++) {
                    if (Utils.rnd.nextInt(100)<50) {
                        this.nextPopulation[child][gene].rotate = this.nextPopulation[ind1][gene].rotate;
                        this.nextPopulation[child][gene].power = this.nextPopulation[ind1][gene].power;
                    } else {
                        this.nextPopulation[child][gene].rotate = this.nextPopulation[ind2][gene].rotate;
                        this.nextPopulation[child][gene].power = this.nextPopulation[ind2][gene].power;
                    }
                }
            }
        }


        // ---------------------------------
        // ---------- MUTATIONS ----------
        // ---------------------------------
        void mutation() {
            for (int child=SELECTED_NB; child<POPULATION_SIZE; child++) {
                for (int gene=0; gene<this.individualEffectiveSize; gene++) {
                    if (Utils.rnd.nextInt(100) < MUTATION_PERCENTAGE) {
                        this.nextPopulation[child][gene].rotate = Utils.randomRotate();
                        this.nextPopulation[child][gene].power = Utils.randomPower();
                    }
                }
            }
        }

        void findTheBestAction() {
            int best = 0;
            int bestFitness = this.fitness[0];
            this.toPlay.rotate = this.currentPopulation[0][0].rotate;
            this.toPlay.power = this.currentPopulation[0][0].power;
            for (int i=0; i<POPULATION_SIZE; i++) {
                if (this.fitness[i] > bestFitness) {
                    this.toPlay.rotate = this.currentPopulation[i][0].rotate;
                    this.toPlay.power = this.currentPopulation[i][0].power;
                    bestFitness = this.fitness[i];
                    best = i;
                }
                System.err.print(this.fitness[i]+";");
            }
            // We keep the toPlay sequence for the next turn.
            Utils.copyContent(this.currentPopulation[best], this.elite, this.individualEffectiveSize);
            System.err.println("\nbest fitness = "+bestFitness+" : "+Utils.toString(this.finalShuttleCurPop[best]));
            System.err.println("seq :"+Utils.toString(this.elite));
        }

        void generateInitialPopulation() {
            for (int i=0; i<POPULATION_SIZE; i++) {
                for (int j=0; j<this.individualEffectiveSize; j++) {
                    this.currentPopulation[i][j].rotate = Utils.randomRotate();
                    this.currentPopulation[i][j].power  = Utils.randomPower();
                }
            }
        }

        void reinjectTheBestIndividual() {
            //System.err.println(">>> reinjectTheBestIndividual ("+ELITE_REINJECT_NB+" times):");
            for (int i=0; i<ELITE_REINJECT_NB; i++) {
                for (int gene = 0; gene<this.elite.length-1; gene++) {
                    this.currentPopulation[i][gene].power = this.elite[gene+1].power;
                    this.currentPopulation[i][gene].rotate = this.elite[gene+1].rotate;
                }
            }
        }

    }

    static class Point {
        double x;
        double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Control {
        int rotate;
        int power;
    }

    static class Shuttle {

        final Point pos;
        double hSpeed;
        double vSpeed;
        int fuel;
        int rotate;
        int power;
        Status status;

        Shuttle() {
            this.pos = new Point(0,0);
        }

        void copyContentOf(final Shuttle s) {
            this.pos.x = s.pos.x;
            this.pos.y = s.pos.y;
            this.hSpeed = s.hSpeed;
            this.vSpeed = s.vSpeed;
            this.fuel = s.fuel;
            this.rotate = s.rotate;
            this.power = s.power;
            this.status = s.status;
        }

    }

    enum Status {
        FLYING, CRASHED, LANDED
    }


    static class Surface {

        final int nbPoints;
        Point[] points;

        int flatMinX;
        int flatMaxX;
        int flatMidX;
        int flatY;

        int flatZoneStartingIndex;

        Surface(Point[] pts, int nbPoints) {
            this.points = pts;
            this.nbPoints = nbPoints;
        }


        void build() {
            int prevY = -1;
            for (int i=0; i<nbPoints; i++) {
                if (points[i].y == prevY) {
                    flatMinX = (int) points[i-1].x;
                    flatMaxX = (int) points[i].x;
                    flatY = (int) points[i].y;
                    flatMidX = ( flatMinX + flatMaxX) / 2;
                    this.flatZoneStartingIndex = i-1;
                    break;
                }
                prevY = (int) points[i].y;
            }

        }

        boolean collideWithGround(final Point p1, final Point p2) {
            if ( !isIn(p2) ) return true;
            boolean col = false;
            int seg = 0;
            do {
                if (Geometry.intersect(p1, p2, points[seg], points[seg+1])) {
                    col = true;
                }
                seg++;
            } while (!col && seg < nbPoints-1);
            return col;
        }

        static boolean isIn(final Point p) {
            return p.x < 7000 && p.x > 0 && p.y < 3000;
        }

        double horizontalDistanceFromFlatZone(Point p) {
            double hDist = 0;
            if ( p.x < flatMinX ) {
                hDist = flatMinX - p.x + 50; // TODO cheater ?
            } else if ( p.x > flatMaxX ) {
                hDist = p.x - flatMaxX + 50; // TODO cheater ?
            }
            return hDist;
        }

        double distanceFromFlatZone(Point p) {
            double dist, hDist = 0, vDist;
            if ( p.x < flatMinX ) {
                hDist = flatMinX - p.x + 50; // TODO cheater ?
            } else if ( p.x > flatMaxX ) {
                hDist = p.x - flatMaxX + 50; // TODO cheater ?
            }
            vDist = p.y-flatY;
            dist = Math.sqrt((hDist*hDist)+(vDist*vDist));
            return dist;
        }

        boolean isAlignWithTheFlatZone(Point p) {
            return p.y < 3000 && p.x > flatMinX + 50 && p.x < flatMaxX - 50;
        }

        boolean collideWithFlatZone(Point p1, Point p2) {
            return Geometry.intersect(p1,p2, points[flatZoneStartingIndex], points[flatZoneStartingIndex+1]);
        }
    }

    static class Utils {

        static final Random rnd = new Random(666);

        //static final DecimalFormat FMT_0_DEC = new DecimalFormat("#0");
        static final DecimalFormat FMT_3_DEC = new DecimalFormat("#0.000");

        //static int[] rotate = new int[]{-90,-75,-60,-45,-30,-15,0,15,30,45,60,75,90};
        static int[] rotate = new int[]{-90,0,90};
        //static int[] power = new int[]{0,1,2,3,4};
        static int[] power = new int[]{0,4,4};

        static int randomRotate() {
            return rotate[rnd.nextInt(rotate.length)];
        }

        static int randomPower() {
            return power[rnd.nextInt(power.length)];
        }

        static void copyContent(Control[] from, Control[] to, int length) {
            for (int i=0; i<length; i++) {
                to[i].power = from[i].power;
                to[i].rotate = from[i].rotate;
            }
        }

        static String toString(int[] array) {
            StringBuilder toRet = new StringBuilder();
            for (int i=0; i<array.length; i++) {
                toRet.append(array[i]).append(' ');
            }
            return toRet.toString();
        }

        static String toString(Control[] ctrl) {
            StringBuilder toRet = new StringBuilder();
            for (int i=0; i<ctrl.length; i++) {
                toRet.append("(r=").append(ctrl[i].rotate).append(";p=").append(ctrl[i].power).append(")");
            }
            return toRet.toString();
        }

        static String toString(Surface surf) {
            StringBuilder toRet = new StringBuilder();
            for (int i=0; i<surf.nbPoints; i++) {
                toRet.append("new Player.Point(").append(surf.points[i].x).append(',').append(surf.points[i].y).append(")\n");

            }
            return toRet.toString();
        }

        static String toString(final Shuttle s) {
            return "{" +
                    "x="+FMT_3_DEC.format(s.pos.x)+",y="+FMT_3_DEC.format(s.pos.y)+
                    ",hs="+FMT_3_DEC.format(s.hSpeed)+",vs="+FMT_3_DEC.format(s.vSpeed)+
                    ",fuel="+s.fuel+
                    ",r="+s.rotate+",p="+s.power+
                    ",status="+s.status+'}';
        }

        static double abs(double a) {
            return (a < 0) ? -a : a;
        }

        static double abs(int a) {
            return (a < 0) ? -a : a;
        }

        static int min(int a, int b) {
            return (a <= b) ? a : b;
        }

        static double min(double a, double b) {
            return (a <= b) ? a : b;
        }

        static int max(int a, int b) {
            return (a >= b) ? a : b;
        }

        static double max(double a, double b) {
            return (a >= b) ? a : b;
        }

    }

    static class Geometry {


        // The main function that returns true if line segment ‘p1q1’
        // and ‘p2q2’ intersect.
        static boolean intersect(Point p1, Point q1, Point p2, Point q2)
        {
            // Find the four orientations needed for general and
            // special cases
            int o1 = orientation(p1, q1, p2);
            int o2 = orientation(p1, q1, q2);
            int o3 = orientation(p2, q2, p1);
            int o4 = orientation(p2, q2, q1);
            // General case
            if (o1 != o2 && o3 != o4)
                return true;
            // Special Cases
            // p1, q1 and p2 are colinear and p2 lies on segment p1q1
            if (o1 == 0 && onSegment(p1, p2, q1)) return true;
            // p1, q1 and p2 are colinear and q2 lies on segment p1q1
            if (o2 == 0 && onSegment(p1, q2, q1)) return true;
            // p2, q2 and p1 are colinear and p1 lies on segment p2q2
            if (o3 == 0 && onSegment(p2, p1, q2)) return true;
            // p2, q2 and q1 are colinear and q1 lies on segment p2q2
            if (o4 == 0 && onSegment(p2, q1, q2)) return true;
            return false; // Doesn’t fall in any of the above cases
        }

        // Given three colinear points p, q, r, the function checks if
        // point q lies on line segment ‘pr’
        static boolean onSegment(Point p, Point q, Point r)
        {
            if (q.x <= Utils.max(p.x, r.x) && q.x >= Utils.min(p.x, r.x) &&
                    q.y <= Utils.max(p.y, r.y) && q.y >= Utils.min(p.y, r.y))
                return true;
            return false;
        }

        // To find orientation of ordered triplet (p, q, r).
        // The function returns following values
        // 0 –> p, q and r are colinear
        // 1 –> Clockwise
        // 2 –> Counterclockwise
        static int orientation(Point p, Point q, Point r)
        {
            // See 10th slides from following link for derivation of the formula
            // http://www.dcs.gla.ac.uk/~pat/52233/slides/Geometry1x1.pdf
            double val = (q.y - p.y) * (r.x - q.x) -
                    (q.x - p.x) * (r.y - q.y);
            if (val == 0) return 0;  // colinear
            return (val > 0) ? 1 : 2; // clock or counterclock wise
        }


    }

}