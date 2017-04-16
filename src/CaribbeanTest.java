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
    public void test_best_path() {
        //TODO: make a test case for bestPath
    }

}
