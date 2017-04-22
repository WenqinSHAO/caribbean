import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

class Ship extends Entity {

    public static final int MAX_SHIP_SPEED = 2;
    public static final int MAX_SHIP_QUANT = 100;

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

    public void applyAction(MoveAction move) {
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
        HashMap<ShipMoveStatus, Integer> gain = new HashMap<>();
        HashMap<Ship, StatusActionPair> lastHop = new HashMap<>();
        PriorityQueue<StatusPriorityPair> frontier = new PriorityQueue<>(10);
        boolean reached = false;

        gain.put(this.getMoveStatus(), 0);
        lastHop.put(this, null);
        int iniPriority = 0 - this.getCoord().distance(target);
        frontier.add(new StatusPriorityPair(this, iniPriority, 0));

        Ship st = this;
        StatusPriorityPair topSP = new StatusPriorityPair(this, iniPriority, 0);
        while (!frontier.isEmpty()) {
            // get the Ship status with highest priority
            topSP = frontier.poll();
            st = topSP.getStatus();
            // if the Ship status overlaps with target, then regarded as arrived
            if (st.overlap(target)) {
                reached = true;
                break;
            }
            for (MoveAction mv : MoveAction.values()) { // iterate over all possible moves for the next turn
                Ship nst = new Ship(st);
                nst.damage(1); // constant cost each turn
                nst.applyAction(mv); // update ship speed and direction based on move
                for (Cannonball b : balls) { // update cannon ball's remaining turn by the top Ship status turn count from initial position
                    b.setRemainingTurns(b.getInitialRemainingTurns()-topSP.getTurnCounter() - 1);
                }
                // in move() and rotate(), the damage related to cannon ball is calculated from remainingTurns instead of initialRemainingTurns
                nst.move(ships, mines, rums, balls);
                nst.rotate(ships, mines, rums, balls);
                if (nst.quant > 0) { // only continue if the remaining rum number is positive
                    int nstGain = nst.quant - this.quant; // the gain in terms of rum after move
                    if (!gain.containsKey(nst.getMoveStatus()) || nstGain > gain.get(nst.getMoveStatus())) {
                        gain.put(nst.getMoveStatus(), nstGain);
                        lastHop.put(nst, new StatusActionPair(st, mv));
                        int priority = nstGain - nst.getCoord().distance(target);
                        frontier.add(new StatusPriorityPair(nst, priority, topSP.getTurnCounter()+1));
                    }
                }
            }
        }

        if (reached) {
            int bestGain = gain.get(st.getMoveStatus());
            List<MoveAction> moves = new ArrayList<>();
            while (lastHop.get(st) != null) {
                moves.add(lastHop.get(st).getAction());
                st = lastHop.get(st).getStatus();
            }
            Collections.reverse(moves);
            return new MoveSequence(bestGain, moves);
        } else {
            return new MoveSequence(0, new ArrayList<>());
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
