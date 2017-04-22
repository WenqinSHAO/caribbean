import java.util.*;

enum MoveAction {
    FASTER, SLOWER, PORT, STARBOARD, WAIT
}

class MoveActions {
    // a combination of 3 move actions
    private MoveAction[] actions = new MoveAction[3];

    MoveActions(final MoveAction[] actions) {
        if (actions.length != 3) {
            throw new IllegalArgumentException();
        }
        this.actions[0] = actions[0];
        this.actions[1] = actions[1];
        this.actions[2] = actions[2];
    }

    public MoveAction get(int ind) {
        return actions[ind];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoveActions that = (MoveActions) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(actions);
    }
}

enum AttackAction {
    MINE, FIRE
}

class CubicCoord {

    private int x;
    private int y;
    private int z;

    public CubicCoord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int distance(CubicCoord t) {
        return (Math.abs(x - t.x) + Math.abs(y - t.y) + Math.abs(z - t.z)) / 2;
    }
}

class OffsetCoord {

    // "odd-r" horizontal layout
    private final static int[][] DIRECTIONS_EVEN = new int[][]{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private final static int[][] DIRECTIONS_ODD = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
    public static final int MAP_WIDTH = 23;
    public static final int MAP_HEIGHT = 21;
    public static OffsetCoord MAP_CENTER = new OffsetCoord(MAP_WIDTH / 2, MAP_HEIGHT / 2);

    private int col;
    private int row;

    public OffsetCoord(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public OffsetCoord(final OffsetCoord coord) {
        this.col = coord.col;
        this.row = coord.row;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OffsetCoord that = (OffsetCoord) o;

        if (col != that.col) return false;
        return row == that.row;
    }

    @Override
    public int hashCode() {
        int result = col;
        result = 31 * result + row;
        return result;
    }

    public OffsetCoord neighbor(int orientation) {
        int newRow, newCol;
        if (this.row % 2 == 1) {
            newRow = this.row + DIRECTIONS_ODD[orientation][1];
            newCol = this.col + DIRECTIONS_ODD[orientation][0];
        } else {
            newRow = this.row + DIRECTIONS_EVEN[orientation][1];
            newCol = this.col + DIRECTIONS_EVEN[orientation][0];
        }
        return new OffsetCoord(newCol, newRow);
    }

    public boolean isInsideMap() {
        return col >= 0 && col < MAP_WIDTH && row >= 0 && row < MAP_HEIGHT;
    }

    public CubicCoord toCubic() {
        int x = this.col - (this.row - (this.row & 1)) / 2;
        int z = this.row;
        int y = -x - z;
        return new CubicCoord(x, y, z);
    }

    public int distance(OffsetCoord t) {
        return toCubic().distance(t.toCubic());
    }

    @Override
    public String toString() {
        return "OffsetCoord{" +
                "col=" + col +
                ", row=" + row +
                '}';
    }
}

class MoveSequence {
    private int gain;
    private List<MoveAction> moves;

    public MoveSequence(int gain, List<MoveAction> moves) {
        this.gain = gain;
        this.moves = moves;
    }

    public int getGain() {
        return gain;
    }

    public List<MoveAction> getMoves() {
        return moves;
    }
}

class StatusActionPair {
    private Ship status;
    private MoveAction action;

    public StatusActionPair(Ship status, MoveAction action) {
        this.status = status;
        this.action = action;
    }

    public Ship getStatus() {
        return status;
    }

    public MoveAction getAction() {
        return action;
    }
}

class StatusPriorityPair implements Comparable<StatusPriorityPair> {
    private Ship status;
    private int priority;
    private int turnCounter;

    public StatusPriorityPair(Ship status, int priority, int turnCounter) {
        this.status = status;
        this.priority = priority;
        this.turnCounter = turnCounter;
    }

    public Ship getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public void setTurnCounter(int turnCounter) {
        this.turnCounter = turnCounter;
    }

    @Override
    public int compareTo(StatusPriorityPair o) {
        if (getPriority() > o.getPriority()) {
            return -1;
        }
        if (getPriority() < o.getPriority()) {
            return 1;
        }
        return 0;
    }
}

class Entity {

    private int id;
    private OffsetCoord location;

    public Entity(int id, int col, int row) {
        this.id = id;
        this.location = new OffsetCoord(col, row);
    }

    public Entity(int id, final OffsetCoord loc) {
        this.id = id;
        this.location = loc;
    }

    public int getId() {
        return id;
    }

    public int getCol() {
        return location.getCol();
    }

    public int getRow() {
        return location.getRow();
    }

    public void setLocation(int col, int row) {
        this.location = new OffsetCoord(col, row);
    }

    public void setLocation(final OffsetCoord loc) {
        this.location = loc;
    }

    public OffsetCoord getCoord() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (id != entity.id) return false;
        return location.equals(entity.location);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + location.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", location=" + location +
                '}';
    }
}

class Rum extends Entity {

    private int quant;

    public Rum(int id, int col, int row, int quant) {
        super(id, col, row);
        this.quant = quant;
    }

    public int getQuant() {
        return quant;
    }
}

class Mine extends Entity {
    public static final int MINE_DAMAGE = 25;
    public static final int NEAR_MINE_DAMAGE = 10;

    public Mine(int id, int col, int row) {
        super(id, col, row);
    }
}

class Cannonball extends Entity {

    private int initialRemainingTurns;
    private int remainingTurns;
    private int ownerID;

    public Cannonball(int id, int col, int row, int iniT, int ownerID) {
        super(id, col, row);
        this.initialRemainingTurns = iniT;
        this.ownerID = ownerID;
        this.remainingTurns = -1;
    }

    public Cannonball(int id, OffsetCoord loc, int iniT, int ownerID) {
        super(id, loc);
        this.initialRemainingTurns = iniT;
        this.ownerID = ownerID;
        this.remainingTurns = -1;
    }

    public int getInitialRemainingTurns() {
        return initialRemainingTurns;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setRemainingTurns(int remainingTurns) {
        this.remainingTurns = remainingTurns;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }
}


class ShipMoveStatus extends Entity {
    private int speed;
    private int direction;

    public ShipMoveStatus(int id, int col, int row, int speed, int direction) {
        super(id, col, row);
        this.speed = speed;
        this.direction = direction;
    }

    public ShipMoveStatus(int id, OffsetCoord loc, int speed, int direction) {
        super(id, loc);
        this.speed = speed;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ShipMoveStatus that = (ShipMoveStatus) o;

        if (speed != that.speed) return false;
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + speed;
        result = 31 * result + direction;
        return result;
    }
}


class Player {

    private static List<MoveActions> allActions = new ArrayList<>(); // All combination of move actions
    private List<Rum> rums = new ArrayList<>();
    private List<Ship> ourships = new ArrayList<>();
    private List<Ship> otherships = new ArrayList<>();
    private List<Mine> mines = new ArrayList<>();
    private List<Cannonball> cannonballs = new ArrayList<>();

    private static void computeActions(int remain, MoveAction[] path) {
        if (remain == 0) {
            allActions.add(new MoveActions(path));
        }
        else {
            for (MoveAction action : MoveAction.values()) {
                path[path.length - remain] = action;
                computeActions(remain - 1, path);
            }
        }
    }

    public static List<MoveActions> getAllActions() {
        if (allActions.isEmpty()) {
            computeActions(3, new MoveAction[3]);
        }
        return allActions;
    }

    public void addRum(Rum rum) {
        rums.add(rum);
    }

    public void addOurShip(Ship ship) {
        ourships.add(ship);
    }

    public void addEnemyShip(Ship ship) {
        otherships.add(ship);
    }

    public void addMine(Mine mine) {
        mines.add(mine);
    }

    public void addCannonball(Cannonball cannonball) {
        cannonballs.add(cannonball);
    }

    public void clearEntities() {
        rums.clear();
        ourships.clear();
        otherships.clear();
        mines.clear();
        cannonballs.clear();
    }

    public List<Rum> getRums() {
        return rums;
    }

    public List<Ship> getOurShips() {
        return ourships;
    }

    public List<Ship> getEnemyShips() {
        return otherships;
    }

    public List<Mine> getMines() {
        return mines;
    }

    public List<Cannonball> getCannonballs() {
        return cannonballs;
    }

    public int getOurShipCount() {
        return ourships.size();
    }

    private class SearchNode {
        private List<Ship> shipStates;
        private SearchNode prev;
        private int score; // total rum

        public SearchNode(final List<Ship> shipStates, final SearchNode prev) {
            this.shipStates = shipStates;
            this.prev = prev;
            this.score = 0;
        }

        public List<Ship> getShipStates() {
            return shipStates;
        }

        public SearchNode getPrev() {
            return prev;
        }

        public void computeScore() {

        }

        public int getScore() {
            return score;
        }
    }

    SearchNode findBest(final List<Ship> actualState) {
        SearchNode current = new SearchNode(actualState, null);
    }

    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        for (Ship ship : this.getOurShips()) {
            int maxGain = Integer.MIN_VALUE;
            List<MoveAction> bestMv = new ArrayList<>();
            for (Rum rum : this.getRums()) {
                MoveSequence mv = ship.bestPath(rum.getCoord(), this.getEnemyShips(), this.getRums(), this.getMines(), this.getCannonballs());
                if (mv.getGain() > maxGain) {
                    maxGain = mv.getGain();
                    bestMv = mv.getMoves();
                }
            }
            if (maxGain < 0) {
                commands.add("MOVE " + OffsetCoord.MAP_CENTER.getCol() + " " + OffsetCoord.MAP_CENTER.getRow());
            } else {
                commands.add(bestMv.get(0).name()); // Any valid action, such as "WAIT" or "MOVE x y"
            }
        }
        return commands;
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        Player player = new Player();
        // game loop
        while (true) {
            player.clearEntities();
            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                switch (entityType) {
                    case "SHIP":
                        if (arg4 == 1) {
                            player.addOurShip(new Ship(entityId, x, y, arg4, arg3, arg2, arg1));
                        } else {
                            player.addEnemyShip(new Ship(entityId, x, y, arg4, arg3, arg2, arg1));
                        }
                        break;
                    case "BARREL":
                        player.addRum(new Rum(entityId, x, y, arg1));
                        break;
                    case "MINE":
                        player.addMine(new Mine(entityId, x, y));
                        break;
                    case "CANNONBALL":
                        player.addCannonball(new Cannonball(entityId, x, y, arg2, arg1));
                        break;
                }
            }

            List<String> commands = player.getCommands();

            for (String command : commands) {
                System.out.println(command);
            }
        }
    }

}
