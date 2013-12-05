package javabot.controllers;


import javabot.models.Unit;


public class TrashManager implements Manager {
        private static TrashManager instance = null;
        
        private int actCount = 0;
        private int msgCount = 5;
        
        private String[] messages = new String[msgCount];
        messages[0] = "Sup Nick.";
        messages[1] = "Ready for some embarassment?";
        messages[2] = "We're gonna send you to the stone age.";
        messages[3] = "Yo Mama so far she can turn a Creep Colony into a Sunken Colony.";
        messages[4] = "Go step on a lego.";
        
        private TrashManager() {
                
        }
        
        public static TrashManager getInstance() {
                if(instance == null) {
                        instance = new TrashManager();
                }
                return instance;
        }


        @Override
        public void act() {
                // TODO Auto-generated method stub
                actCount++;
                
                if ((actCount % 5) == 0) {
                        int msg = actCount / 5;
                        if (msg < msgCount) {
                                printMessage(messages[msg]);
                        }
                }
        }


        @Override
        public void gameUpdate() {
                // TODO Auto-generated method stub
                
        }


        @Override
        public void assignUnit(Unit unit) {
                // TODO Auto-generated method stub
                
        }


        @Override
        public int removeUnit(int unitId) {
                // TODO Auto-generated method stub
                return -1;
        }
        
        public void printMessage(String msg) {
       		JavaBot.bwapi.printText(msg);
        }


}
