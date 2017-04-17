import java.util.*;

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
    public static OffsetCoord MAP_CENTER = new OffsetCoord(MAP_WIDTH/2, MAP_HEIGHT/2);

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
    private List<Ship.Action> moves;

    public MoveSequence(int gain, List<Ship.Action> moves) {
        this.gain = gain;
        this.moves = moves;
    }

    public int getGain() {
        return gain;
    }

    public List<Ship.Action> getMoves() {
        return moves;
    }
}

class StatusActionPair {
    private Ship status;
    private Ship.Action action;

    public StatusActionPair(Ship status, Ship.Action action) {
        this.status = status;
        this.action = action;
    }

    public Ship getStatus() {
        return status;
    }

    public Ship.Action getAction() {
        return action;
    }
}

class StatusPriorityPair implements Comparable<StatusPriorityPair> {
    private Ship status;
    private int priority;

    public StatusPriorityPair(Ship status, int priority) {
        this.status = status;
        this.priority = priority;
    }

    public Ship getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
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
    public Cannonball(int id, int col, int row) {
        super(id, col, row);
    }

    public Cannonball(int id, OffsetCoord loc) {
        super(id, loc);
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

class Ship extends Entity {

    public static final int MAX_SHIP_SPEED = 2;
    public static final int MAX_SHIP_QUANT = 100;

    public static enum Action {
        FASTER, SLOWER, PORT, STARBOARD, MINE, EMPTY// , FIRE,
    }

    private int owner;
    private int quant;
    private int speed;
    private int direction;
    private int newDirection;
    private OffsetCoord newCoord;

    public Ship(int id, int col, int row, int owner, int quant, int speed, int direction) {
        super(id, col, row);
        this.owner = owner;
        this.quant = quant;
        this.speed = speed;
        this.direction = direction;
        this.newCoord = null;
        this.newDirection = -1;
    }

    public Ship(final Ship ship) {
        this(ship.getId(), ship.getCol(), ship.getRow(), ship.getOwner(), ship.getQuant(), ship.getSpeed(), ship.getDirection());
        this.newCoord = ship.newCoord;
        this.newDirection = ship.newDirection;
    }

    private ShipMoveStatus getMoveStatus() {
        return new ShipMoveStatus(this.getId(), this.getCoord(), this.speed, this.direction);
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getQuant() {
        return quant;
    }

    public void setQuant(int quant) {
        this.quant = quant;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getNewDirection() {
        return newDirection;
    }

    public void setNewDirection(int newDirection) {
        this.newDirection = newDirection;
    }

    public OffsetCoord getNewCoord() {
        return newCoord;
    }

    public void setNewCoord(OffsetCoord newLocation) {
        this.newCoord = newLocation;
    }

    public List<OffsetCoord> getPositions() {
        List<OffsetCoord> positions = new ArrayList<>();
        positions.add(getCoord().neighbor(direction));
        positions.add(getCoord());
        positions.add(getCoord().neighbor((direction + 3) % 6));
        return positions;
    }

    /**
     * compute new coordinates based on new direction and new position
     *
     * @return bow, position and stern positions
     */
    public List<OffsetCoord> getNewPositions() {
        List<OffsetCoord> positions = new ArrayList<>();
        OffsetCoord newCoord = getNewCoord();
        if (newCoord == null) {
            newCoord = getCoord();
        }
        int newOrientation = getNewDirection();
        if (newOrientation == -1) {
            newOrientation = getDirection();
        }
        // The ship has a new position
        positions.add(newCoord.neighbor(newOrientation));
        positions.add(newCoord);
        positions.add(newCoord.neighbor((newOrientation + 3) % 6));
        return positions;
    }

    private void damage(int damage) {
        this.quant -= damage;
        if (this.quant < 0) {
            this.quant = 0;
        }
    }

    private void heal(int val) {
        this.quant += val;
        if (this.quant > MAX_SHIP_QUANT) {
            this.quant = MAX_SHIP_QUANT;
        }
    }

    public void rotate(final Iterable<Ship> ships, final Iterable<Mine> mines, final Iterable<Rum> barrels, final Iterable<Cannonball> cannonballs) {
        if (this.getNewDirection() == -1) {
            return;
        }
        // Check collisions
        boolean collisionDetected = false;

        for (Ship ship : ships) {
            if (newCoordOverlap(ship)) {
                collisionDetected = true;
                break;
            }
        }

        if (collisionDetected) {
            this.setNewDirection(this.getDirection());
            this.setSpeed(0);
        }

        // Apply rotation
        this.setDirection(this.getNewDirection());
        checkCollisions(mines, barrels, cannonballs);
        this.setNewDirection(-1);
    }

    public boolean overlap(OffsetCoord location) {
        List<OffsetCoord> coords = getPositions();
        return coords.contains(location);
    }

    public boolean overlap(Ship entity) {
        if (this.getId() != entity.getId()) {
            List<OffsetCoord> positions = entity.getPositions();
            for (OffsetCoord coord : getPositions()) {
                if (positions.contains(coord)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean newCoordOverlap(Ship entity) {
        if (this.getId() != entity.getId()) {
            List<OffsetCoord> positions = entity.getPositions();
            for (OffsetCoord coord : getNewPositions()) {
                if (positions.contains(coord)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void applyAction(Action move) {
        switch (move) {
            case PORT:
                this.setNewDirection((this.direction + 1) % 6);
                break;
            case STARBOARD:
                this.setNewDirection((this.direction + 6 - 1) % 6);
                break;
            case SLOWER:
                this.speed = Math.max(0, this.speed - 1);
                break;
            case FASTER:
                this.speed = Math.min(2, this.speed + 1);
                break;
            default:
                break;
        }
    }

    public MoveSequence bestPath(OffsetCoord target, Iterable<Ship> ships, Iterable<Rum> rums, Iterable<Mine> mines, Iterable<Cannonball> balls) {
        Hashtable<ShipMoveStatus, Integer> gain = new Hashtable<>();
        Hashtable<Ship, StatusActionPair> lastHop = new Hashtable<>();
        PriorityQueue<StatusPriorityPair> frontier = new PriorityQueue<>(10);
        boolean reached = false;

        gain.put(this.getMoveStatus(), 0);
        lastHop.put(this, new StatusActionPair(this, Action.EMPTY));
        int iniPriority = 0 - this.getCoord().distance(target);
        frontier.add(new StatusPriorityPair(this, iniPriority));

        Ship st = this;
        while (!frontier.isEmpty()) {
            st = frontier.poll().getStatus();
            if (st.overlap(target)) {
                reached = true;
                break;
            }
            for (Action mv : Action.values()) {
                Ship nst = new Ship(st);
                nst.damage(1);
                nst.applyAction(mv);
                nst.move(ships, mines, rums, balls);
                nst.rotate(ships, mines, rums, balls);
                if (nst.quant > 0) {
                    int nstGain = nst.quant - this.quant;
                    if (!gain.containsKey(nst.getMoveStatus()) || nstGain > gain.get(nst.getMoveStatus())) {
                        gain.put(nst.getMoveStatus(), nstGain);
                        lastHop.put(nst, new StatusActionPair(st, mv));
                        int priority = nstGain - nst.getCoord().distance(target);
                        frontier.add(new StatusPriorityPair(nst, priority));
                    }
                }
            }
        }

        if (reached) {
            int bestGain = gain.get(st.getMoveStatus());
            List<Action> moves = new ArrayList<>();
            while (lastHop.get(st).getAction() != Action.EMPTY) {
                moves.add(lastHop.get(st).getAction());
                st = lastHop.get(st).getStatus();
            }
            Collections.reverse(moves);
            return new MoveSequence(bestGain, moves);
        } else {
            return new MoveSequence(0, new ArrayList<Action>());
        }
    }

    private void checkCollisions(final Iterable<Mine> mines, final Iterable<Rum> barrels, final Iterable<Cannonball> cannonballs) {

        List<OffsetCoord> positions = getPositions();
        // Compute potential gains
        for (Iterator<Rum> it = barrels.iterator(); it.hasNext(); ) {
            Rum barrel = it.next();
            if (positions.contains(barrel.getCoord())) { // not using overlap to improve performance
                heal(barrel.getQuant());
            }
        }

        // Compute potential losses
        for (Iterator<Mine> it = mines.iterator(); it.hasNext(); ) {
            Mine mine = it.next();
            OffsetCoord coord = mine.getCoord();
            if (positions.contains(coord)) {
                damage(Mine.MINE_DAMAGE);
            }
            // TODO: to see whether mine will explode
        }
    }

    public void move(final Iterable<Ship> ships, final Iterable<Mine> mines, final Iterable<Rum> barrels, final Iterable<Cannonball> cannonballs) {

        for (int i = 1; i <= MAX_SHIP_SPEED; i++) {
            if (i > this.getSpeed()) {
                continue;
            }

            this.setNewCoord(this.getCoord());
            // Compute new location
            OffsetCoord newCoord = this.getCoord().neighbor(this.getDirection());

            // If go out of the map, then stay at old location
            if (newCoord.isInsideMap()) {
                this.setNewCoord(newCoord);
            } else {
                this.setSpeed(0);
            }

            // Check the current ship collides with other ships
            // If collides with other ship
            // stay at old location
            for (Ship s : ships) {
                if (this.overlap(s)) {
                    this.setNewCoord(getCoord());
                    this.setSpeed(0);
                    break;
                }
            }

            this.setLocation(this.getNewCoord());
            this.checkCollisions(mines, barrels, cannonballs);

            this.setNewCoord(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Ship ship = (Ship) o;

        if (owner != ship.owner) return false;
        if (quant != ship.quant) return false;
        if (speed != ship.speed) return false;
        if (direction != ship.direction) return false;
        if (newDirection != ship.newDirection) return false;
        return newCoord != null ? newCoord.equals(ship.newCoord) : ship.newCoord == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + owner;
        result = 31 * result + quant;
        result = 31 * result + speed;
        result = 31 * result + direction;
        result = 31 * result + newDirection;
        result = 31 * result + (newCoord != null ? newCoord.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "owner=" + owner +
                ", quant=" + quant +
                ", speed=" + speed +
                ", direction=" + direction +
                "} " + super.toString();
    }
}


class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            // status
            List<Rum> rums = new ArrayList<>();
            List<Ship> ourships = new ArrayList<>();
            List<Ship> otherships = new ArrayList<>();
            List<Mine> mines = new ArrayList<>();
            List<Cannonball> cannonballs = new ArrayList<>();

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
                            ourships.add(new Ship(entityId, x, y, arg4, arg3, arg2, arg1));
                        } else {
                            otherships.add(new Ship(entityId, x, y, arg4, arg3, arg2, arg1));
                        }
                        break;
                    case "BARREL":
                        rums.add(new Rum(entityId, x, y, arg1));
                        break;
                    case "MINE":
                        mines.add(new Mine(entityId, x, y));
                        break;
                    case "CANNONBALL":
                        break;
                }
            }

            for (int i = 0; i < myShipCount; i++) {
                Ship ship = ourships.get(i);
                int maxGain = Integer.MIN_VALUE;
                List<Ship.Action> bestMv = new ArrayList<>();
                for (Rum rum : rums) {
                    MoveSequence mv = ship.bestPath(rum.getCoord(), otherships, rums, mines, cannonballs);
                    if (mv.getGain() > maxGain) {
                        maxGain = mv.getGain() + rum.getQuant();
                        bestMv = mv.getMoves();
                    }
                }
                if (maxGain < 0) {
                    System.out.println("MOVE " + OffsetCoord.MAP_CENTER.getCol() + " " + OffsetCoord.MAP_CENTER.getRow());
                } else {
                    System.out.println(bestMv.get(0)); // Any valid action, such as "WAIT" or "MOVE x y"
                }
            }
        }
    }
}
