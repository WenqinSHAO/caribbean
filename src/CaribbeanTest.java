import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CaribbeanTest {

    List<Ship> ships;
    List<Mine> mines;
    List<Rum> barrels;
    List<Cannonball> cannonballs;

    private void addShip(Ship ship) {
        ships.add(ship);
    }

    private void addMine(Mine mine) {
        mines.add(mine);
    }

    private void addCannonball(Cannonball cannonball) {
        cannonballs.add(cannonball);
    }

    @Before
    public void setUp() throws Exception {
        ships = new ArrayList<>();
        mines = new ArrayList<>();
        barrels = new ArrayList<>();
        cannonballs = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_coord() {
        OffsetCoord coord = new OffsetCoord(16, 10);
        OffsetCoord coord1 = new OffsetCoord(11, 10);
        OffsetCoord coord2 = new OffsetCoord(12, 10);
        assertEquals(4, coord.distance(coord2));
        assertEquals(5, coord.distance(coord1));

        coord = new OffsetCoord(0, 1);
        coord1 = new OffsetCoord(1, 3);
        assertEquals(2, coord.distance(coord1));

        coord1 = new OffsetCoord(2, 3);
        assertEquals(3, coord.distance(coord1));

        coord1 = new OffsetCoord(3, 4);
        assertEquals(4, coord.distance(coord1));
    }

    @Test
    public void test_ship_faster() {
        Ship ship = new Ship(0, 11, 10, 0, 50, 1, 0);
        addShip(ship);
        Ship expect = new Ship(ship);
        expect.setSpeed(2);
        ship.applyAction(Ship.Action.FASTER);
        assertEquals(expect, ship);
        ship.move(ships, mines, barrels, cannonballs);
        expect.setDirection(0);
        expect.setLocation(13, 10);
        expect.setQuant(50);
        assertEquals(expect, ship);
        ship.rotate(ships, mines, barrels, cannonballs);
        assertEquals(expect, ship);

        ship = new Ship(0, 11, 10, 0, 50, 2, 0);
        expect = new Ship(ship);
        ship.applyAction(Ship.Action.FASTER);
        assertEquals(expect, ship);
    }

    @Test
    public void test_ship_slower() {
        Ship ship = new Ship(0, 11, 10, 0, 50, 2, 0);
        Ship expect = new Ship(ship);
        expect.setSpeed(1);
        ship.applyAction(Ship.Action.SLOWER);
        assertEquals(expect, ship);
        ship.move(ships, mines, barrels, cannonballs);
        expect.setDirection(0);
        expect.setLocation(12, 10);
        expect.setQuant(50);
        assertEquals(expect, ship);
        ship.rotate(ships, mines, barrels, cannonballs);
        assertEquals(expect, ship);

        ship = new Ship(0, 11, 10, 0, 50, 0, 0);
        expect = new Ship(ship);
        expect.setSpeed(0);
        ship.applyAction(Ship.Action.SLOWER);
        assertEquals(expect, ship);
    }

    @Test
    public void test_port() {
        Ship ship = new Ship(0, 11, 10, 0, 50, 2, 0);
        ship.applyAction(Ship.Action.PORT);
        assertEquals(0, ship.getDirection());
        ship.move(ships, mines, barrels, cannonballs);
        ship.rotate(ships, mines, barrels, cannonballs);
        Ship exp = new Ship(ship);
        exp.setLocation(13, 10);
        exp.setDirection(1);
        assertEquals(exp, ship);
    }

    @Test
    public void test_starboard() {
        Ship ship = new Ship(0, 11, 10, 0, 50, 2, 0);
        ship.applyAction(Ship.Action.STARBOARD);
        assertEquals(0, ship.getDirection());
        ship.move(ships, mines, barrels, cannonballs);
        ship.rotate(ships, mines, barrels, cannonballs);
        Ship exp = new Ship(ship);
        exp.setLocation(13, 10);
        exp.setDirection(5);
        assertEquals(exp, ship);
    }

    @Test
    public void test_path_simple() {
        Ship ship = new Ship(0, 11, 10, 0, 50, 0, 0);
        Rum rum = new Rum(1, 16, 10, 30);
        /*
        (11, 10) - FASTER - (12, 10) - FASTER - (14, 10) - WAIT - (16, 10)
         */
        MoveSequence path = ship.bestPath(rum.getCoord(), ships, barrels, mines, cannonballs);
        List<Ship.Action> moves = path.getMoves();
        assertEquals(3, moves.size());

        ship = new Ship(0, 1, 10, 0, 50, 0, 0);
        rum = new Rum(1, 1, 11, 30);
        /*
        BOARD
         */
        path = ship.bestPath(rum.getCoord(), ships, barrels, mines, cannonballs);
        moves = path.getMoves();
        assertEquals(1, moves.size());
        assertEquals(Ship.Action.STARBOARD, moves.get(0));

        // Ship has an initial speed of 1
        ship = new Ship(0, 1, 10, 0, 50, 1, 0);
        rum = new Rum(1, 1, 11, 30);
        /*
        BOARD
         */
        path = ship.bestPath(rum.getCoord(), ships, barrels, mines, cannonballs);
        moves = path.getMoves();
        assertEquals(1, moves.size());
        assertEquals(Ship.Action.PORT, moves.get(0));
    }

    @Test
    public void test_best_path() {
        //TODO: make a test case for bestPath
    }

    @Test
    public void test_avoid_cannonball() {
        Player p = new Player();
        p.addOurShip(new Ship(0, 22, 18, 1, 75, 1, 2));
        p.addEnemyShip(new Ship(1, 21, 16, 0, 11, 1, 0));
        p.addCannonball(new Cannonball(58, 20, 19, 1, 1));
        p.addMine(new Mine(39, 20, 15));
        List<String> commands = p.getCommands();
    }
}
