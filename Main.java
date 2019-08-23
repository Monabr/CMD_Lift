

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

interface ILiftListener {
    void actionLiftRefresh();
}

interface IHumansListener {
    void actionHumansAtFloorRefresh(int currentFloor);
}

class Human {
    private int toFloor;

    public Human (int contOfFloors, int currentFloor){
        for (int i = 0; true; i++) {
            toFloor = (int)((Math.random()*contOfFloors-1)+1);
            if(toFloor != currentFloor) {
                break;
            }
        }
    }

    public int getToFloor() {
        return toFloor;
    }

}

class Lift{
    ILiftListener listener;
    IHumansListener humansListener;

    private final int maxPeople = 5;

    private int currentFloor;
    private int targetFloor;
    private int countOfFloors;

    private ArrayList<Human> humansInLift;

    private House house;

    public Lift (House house, int contOfFloors) {
        this.countOfFloors = contOfFloors;
        humansInLift = new ArrayList<Human>();
        this.house = house;
        currentFloor = 0;
        targetFloor = 0;
    }

    public void addListener(ILiftListener listener) {
        this.listener = listener;
    }

    public void addListener(IHumansListener listener) {
        this.humansListener = listener;
    }

    public ArrayList<Human> getHumansInLift() {
        return humansInLift;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void startWorking () {
        if(tryToGetMoreHumans()){
            setTargetFloor();
        } else if (humansInLift.size() == 0){
            findNearest ();
        }
        for (int i = 0; house.countAllWaitingHumans() > 0; i++) {
            goToTargetFloor();
        }
    }

    private void goToTargetFloor() {
        for (int i = 0; currentFloor != targetFloor; i++) {
            if (currentFloor < targetFloor) {
                currentFloor++;
            } else if (currentFloor > targetFloor) {
                currentFloor--;
            }
            listener.actionLiftRefresh();
            CheckToOutFromLift();
            if(tryToGetMoreHumans()){
                setTargetFloor();
            } else if (humansInLift.size() == 0){
                findNearest ();
            }
        }
    }

    private void maxFloorBelow(){
        for (int i = 0; i < humansInLift.size(); i++) {
            if(humansInLift.get(i).getToFloor() < targetFloor) {
                targetFloor = humansInLift.get(i).getToFloor();
            }
        }
    }

    private void maxFloorAbove(){
        for (int i = 0; i < humansInLift.size(); i++) {
            if(humansInLift.get(i).getToFloor() > targetFloor) {
                targetFloor = humansInLift.get(i).getToFloor();
            }
        }
    }

    private void setTargetFloor() {
        if(currentFloor == 0){
            maxFloorAbove();
        } else if (currentFloor == (countOfFloors - 1)){
            maxFloorBelow();
        } else if (currentFloor < targetFloor) {
            maxFloorAbove();
        } else if (currentFloor > targetFloor) {
            maxFloorBelow();
        }
    }

    private void getInLiftDown(ArrayList<Human> humansAtFloor) {
        for (int i = 0; (humansInLift.size() < maxPeople) && (humansAtFloor.size() > 0); i++) {
            if(humansAtFloor.get(i).getToFloor() < currentFloor){
                humansInLift.add(humansAtFloor.get(i));
                humansAtFloor.remove(i);
                i--;
                listener.actionLiftRefresh();
            }
        }
    }

    private void getInLiftUp(ArrayList<Human> humansAtFloor) {
        for (int i = 0; (humansInLift.size() < maxPeople) && (humansAtFloor.size() > 0); i++) {
            if(humansAtFloor.get(i).getToFloor() > currentFloor){
                humansInLift.add(humansAtFloor.get(i));
                humansAtFloor.remove(i);
                i--;
                listener.actionLiftRefresh();
            }
        }
    }

    private boolean tryToGetMoreHumans (){
        //decide if lift have enough space and humans waiting at floor
        if ((humansInLift.size() < maxPeople) && (house.getHumansAtFloor(currentFloor).size() > 0)){
            //check to get more humans if we in middle and decide where we will go in top or in bottom
            if((currentFloor == targetFloor) && (currentFloor > 0) && (currentFloor < countOfFloors-1) && (humansInLift.size() == 0)) {
                int up = 0;
                int down = 0;
                ArrayList<Human> humansAtFloor = house.getHumansAtFloor(currentFloor);

                for (int i = 0; i < humansAtFloor.size(); i++) {
                    if(humansAtFloor.get(i).getToFloor() > currentFloor){
                        up++;
                    } else if(humansAtFloor.get(i).getToFloor() < currentFloor){
                        down++;
                    }
                }

                if (up > down) {
                    getInLiftUp(humansAtFloor);
                    maxFloorAbove();
                } else if (up < down) {
                    getInLiftDown(humansAtFloor);
                    maxFloorBelow();
                } else {
                    getInLiftDown(humansAtFloor);
                    maxFloorBelow();
                }
            }
            else {
                return getInLift(house.getHumansAtFloor(currentFloor));
            }

        }
        return false;
    }

    private boolean getInLift (ArrayList<Human> humansAtFloor){
        boolean getNew = false;
        //take new humans if we have space and humans at floor
        for (int i = 0; (humansInLift.size() < maxPeople) && (humansAtFloor.size() > 0) && (i < humansAtFloor.size()); i++) {
            // get more people if we on road to top
            if(targetFloor > currentFloor) {
                if(humansAtFloor.get(i).getToFloor() > currentFloor){
                    humansInLift.add(humansAtFloor.get(i));
                    humansAtFloor.remove(i);
                    i--;
                    getNew = true;
                    listener.actionLiftRefresh();
                }
            } // get more people if we on road to down
            else if (targetFloor < currentFloor) {
                if(humansAtFloor.get(i).getToFloor() < currentFloor){
                    humansInLift.add(humansAtFloor.get(i));
                    humansAtFloor.remove(i);
                    i--;
                    getNew = true;
                    listener.actionLiftRefresh();
                }
            } else {
                humansInLift.add(humansAtFloor.get(i));
                humansAtFloor.remove(i);
                i--;
                listener.actionLiftRefresh();
                getNew = true;
            }

        }
        return getNew;
    }

    private void CheckToOutFromLift(){
        for (int i = 0; i < humansInLift.size(); i++) {
            if (humansInLift.get(i).getToFloor() == currentFloor){
                humansInLift.remove(i);
                i--;
                humansListener.actionHumansAtFloorRefresh(currentFloor);
                listener.actionLiftRefresh();
            }
        }
    }

    private void findNearest () {
        int aboveFloor = findAbove(currentFloor);
        int belowFloor = findBelow(currentFloor);
        int aboveRange = aboveFloor - currentFloor;
        int belowRange = currentFloor - belowFloor;

        if ((aboveFloor > -1) && (belowFloor == -1)){
            targetFloor = aboveFloor;
        } else if ((belowFloor > -1) && (aboveFloor == -1)) {
            targetFloor = belowFloor;
        } else if((aboveFloor == belowFloor) && (aboveFloor == -1)) {
            //here mast me "job done"
        } else if (aboveRange == belowRange) {
            if (house.getHumansAtFloor(aboveFloor).size() == house.getHumansAtFloor(belowFloor).size()) {
                targetFloor = belowRange;
            } else if (house.getHumansAtFloor(aboveFloor).size() > house.getHumansAtFloor(belowFloor).size()) {
                targetFloor = aboveRange;
            } else if (house.getHumansAtFloor(aboveFloor).size() < house.getHumansAtFloor(belowFloor).size()) {
                targetFloor = belowRange;
            }
        } else if ((aboveRange < belowRange) && (aboveFloor < countOfFloors)) {
            targetFloor = aboveFloor;
        } else if ((aboveRange > belowRange) && (belowFloor > -1)) {
            targetFloor = belowFloor;
        }
    }

    private int findAbove (int currentFloor) {
        int above = -1;
        for (int i = currentFloor; i < countOfFloors; i++) {
            if(house.getHumansAtFloor(i).size() > 0) {
                above = i;
            }
        }
        return above;
    }

    private int findBelow (int currentFloor) {
        int below = -1;
        for (int i = currentFloor; i > -1; i--) {
            if(house.getHumansAtFloor(i).size() > 0) {
                below = i;
            }
        }
        return below;
    }
}

class House implements IHumansListener{
    private ArrayList<ArrayList<Human>> floors;

    private Lift lift;
    private Visualiser visualiser;

    public House (){
        int contOfFloors = (int)((Math.random()*15)+5);
        //int contOfFloors = (int)((Math.random()*2)+5);

        floors = new ArrayList<ArrayList<Human>>();
        lift = new Lift(this,contOfFloors);
        Visualiser visualiser = new Visualiser(this);

        lift.addListener(visualiser);
        lift.addListener(this);

        for (int i = 0; i < contOfFloors; i++) {
            ArrayList<Human> temp = new ArrayList<Human>();
            for (int j = 0; j < (int)(Math.random()*10); j++) {
                temp.add(new Human(contOfFloors, i));
            }
            floors.add(temp);
        }
        visualiser.drawHouse();
    }

    public ArrayList<ArrayList<Human>> getFloors() {
        return floors;
    }

    public ArrayList<Human> getHumansAtFloor(int currentFloor) {
        return floors.get(currentFloor);
    }

    public int countAllWaitingHumans () {
        int count = 0;

        for (int i = 0; i <floors.size() ; i++) {
            count += floors.get(i).size();
        }

        return count;
    }

    public void turnOnLift(){
        lift.startWorking();
    }

    public Lift getLift(){
        return this.lift;
    }

    @Override
    public void actionHumansAtFloorRefresh(int currentFloor)
    {
        floors.get(currentFloor).add(new Human(floors.size(), currentFloor));
    }
}

class Visualiser implements ILiftListener{
    private House house;
    private Lift lift;
    private ArrayList<String> floors;

    public Visualiser (House house) {
        this.house = house;
        this.lift = house.getLift();
        floors = new ArrayList<String>();
    }

    private String drawLifr(){
        String s = "(";
        for (int i = 0; i < lift.getHumansInLift().size(); i++) {
           try {
               s += "|"+lift.getHumansInLift().get(i).getToFloor();
           } catch (Exception e) {
               s += "| ";
           }
        }
        if (lift.getHumansInLift().size() > 0) {
            s+="|)";
        } else {
            s+=")";
        }

        for (int i = 0; s.length() < 18; i++) {
            s+=" ";
        }
        return s;
    }

    private String drawFloor(int index){
        String s = "";

        //before lift
        if(index < 10){
            s += "0"+index+"|";
        } else {
            s += ""+index+"|";
        }

        //draw lift or empty
        if (index == lift.getCurrentFloor()) {
            s += drawLifr();
        } else {
            s+= "                  ";
        }

        //draw humans on floor
        s+=":";
        for (int i = 0; i < house.getFloors().get(index).size(); i++) {
            s+= "<"+house.getFloors().get(index).get(i).getToFloor()+">";
        }

        //fill  empty space
        for (int i = 0; s.length() < 100; i++) {
            s+="_";
        }

        return s;
    }



    public void drawHouse () {
        try { Thread.sleep(1000);} catch (Exception e){}
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        for (int i = house.getFloors().size() -1 ; i > -1; i--) {
            System.out.println(drawFloor(i));
        }
        System.out.println("____________________________________________________________________________________________________");
    }


    @Override
    public void actionLiftRefresh() {
        drawHouse();
    }
}

public class Main {
    public static void main(String[] args) {
	    House house = new House();
	    house.turnOnLift();



	    System.out.println("job finished!");
    }
}
