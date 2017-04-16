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

    private final static int[][] DIRECTIONS_EVEN = new int[][]{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private final static int[][] DIRECTIONS_ODD = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
    private static final int MAP_WIDTH = 23;
    private static final int MAP_HEIGHT = 21;

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
        int x = this.col - (this.row - (this.col & 1)) / 2;
        int z = this.row;
        int y = -x - z;
        return new CubicCoord(x, y, z);
    }

    public int distance(OffsetCoord t) {
        return toCubic().distance(t.toCubic());
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

class Ship extends Entity {

    public static final int MAX_SHIP_SPEED = 2;
    public static final int MAX_SHIP_QUANT = 100;
    public static enum Action {
        FASTER, SLOWER, PORT, STARBOARD, FIRE, MINE
    }

    private int owner;
    private int quant;
    private int speed;
    private int direction;

    public Ship(int id, int col, int row, int owner, int quant, int speed, int direction) {
        super(id, col, row);
        this.owner = owner;
        this.quant = quant;
        this.speed = speed;
        this.direction = direction;
    }

    public Ship(final Ship ship) {
        this(ship.getId(), ship.getCol(), ship.getRow(), ship.getOwner(), ship.getQuant(), ship.getSpeed(), ship.getDirection());
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

    public List<OffsetCoord> getPositions() {
        List<OffsetCoord> positions = new ArrayList<>();
        positions.add(getCoord().neighbor(direction));
        positions.add(getCoord());
        positions.add(getCoord().neighbor((direction + 3) % 6));
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

    private void rotate() {

    }

    public boolean overlap(Entity entity) {
        List<OffsetCoord> coords = getPositions();
        return coords.contains(entity.getCoord());
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
    public void applyAction(Action move) {
        switch (move) {
            case PORT:
                this.direction = (this.direction + 1) % 6;
                break;
            case STARBOARD:
                this.direction = (this.direction - 1) % 6;
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

    public List<String> bestPath(OffsetCoord t, List<Mine> mines) {
        // returns a squence of best move (PORT, STARBOARD,...) to reach the give destination t
        // visited = set()
        // queue =  PriorityQueue((self, [empty move], self.distance(t))) distance to t as priority function
        // while queue:
        //     st = queue.pop() gives the status (class Ship) with smallest distance
        //     if st[0].overlap(t):
        //         return st[1] that is the move squence
        //     for move in [PORT, STARBOARD, SLOWER....]:
        //          nst = st[0].nextStatus(move)
        //          if nst not in visited and not nst.overlap() with any m in mines:
        //              queue.add(nst, [empty move, move], nst.distance(t))
        // return 'unreachable'
        return null;
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


    public void move(final Iterable<Ship> ships, final List<Mine> mines, final List<Rum> barrels, final List<Cannonball> cannonballs) {

        for (int i = 1; i <= MAX_SHIP_SPEED; i++) {
            if (i > this.getSpeed()) {
                continue;
            }

            // Compute new location
            OffsetCoord oldLocation = new OffsetCoord(this.getCoord());
            int oldSpeed = this.getSpeed();
            OffsetCoord newLocation = oldLocation.neighbor(this.getDirection());
            int newSpeed = oldSpeed;

            // If go out of the map, then stay at old location
            if (newLocation.isInsideMap()) {
                this.setLocation(newLocation);
                this.setSpeed(newSpeed);
            } else {
                this.setLocation(oldLocation);
                this.setSpeed(0);
            }

            // Check the current ship collides with other ships
            // If collides with other ship
            // stay at old location
            for (Ship s : ships) {
                if (this.overlap(s)) {
                    this.setLocation(oldLocation);
                    this.setSpeed(0);
                    break;
                }
            }

            this.checkCollisions(mines, barrels, cannonballs);

        }
    }

}


class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            // status
            List<Rum> rums = new ArrayList<>();
            List<Ship> ourship = new ArrayList<>();

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
                            ourship.add(new Ship(entityId, x, y, arg4, arg3, arg2, arg1));
                        }
                        break;
                    case "BARREL":
                        rums.add(new Rum(entityId, x, y, arg1));
                        break;
                    case "MINE":
                        break;
                    case "CANNONBALL":
                        break;
                }
            }

            for (int i = 0; i < myShipCount; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");
                int min = Integer.MAX_VALUE;
                Ship ship = ourship.get(i);
                Rum nearest = null;
                for (Rum rum : rums) {
                    int distance = ship.getCoord().distance(rum.getCoord());
                    if (distance < min) {
                        min = distance;
                        nearest = rum;
                    }
                }
                if (nearest != null)
                    System.out.println("MOVE " + nearest.getCol() + " " + nearest.getRow()); // Any valid action, such as "WAIT" or "MOVE x y"
                else
                    System.out.println("WAIT");
            }
        }
    }
}