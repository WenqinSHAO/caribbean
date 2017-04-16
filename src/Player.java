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

    private final static int[][] DIRECTIONS_EVEN = new int[][] { { 1, 0 }, { 0, -1 }, { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, 1 } };
    private final static int[][] DIRECTIONS_ODD = new int[][] { { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, 0 }, { 0, 1 }, { 1, 1 } };
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

    public int distance(Entity t) {
        return toCubic().distance(t.toCubic());
    }

    public OffsetCoord getCoord() {
        return location;
    }

    private CubicCoord toCubic() {
        int x = getCol() - (getRow() - (getCol() & 1)) / 2;
        int z = getRow();
        int y = -x - z;
        return new CubicCoord(x, y, z);
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

class Ship extends Entity {

    public static final int MAX_SHIP_SPEED = 2;
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

}


class Player {

    List<Ship> ships;

    private void moveShip(Ship ship) {
        // ---
        // Go forward
        // ---
        for (int i = 1; i <= Ship.MAX_SHIP_SPEED; i++) {
            if (i > ship.getSpeed()) {
                continue;
            }

            OffsetCoord oldLocation = new OffsetCoord(ship.getCoord());
            int oldSpeed = ship.getSpeed();
            OffsetCoord newLocation = oldLocation.neighbor(ship.getDirection());
            int newSpeed = oldSpeed;

            if (!newLocation.isInsideMap()){
                newLocation = oldLocation;
                newSpeed = 0;
            }

            ship.setLocation(newLocation);
            ship.setSpeed(newSpeed);

            // Check the current ship collides with other ships
            for (Ship s : this.ships) {
                if (ship.overlap(s)) {
                    ship.setLocation(oldLocation);
                    ship.setSpeed(0);
                }
            }
        }
    }

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
                    int distance = ship.distance(rum);
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