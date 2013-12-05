package javabot.controllers;


import javabot.models.Unit;


public class TrashManager implements Manager {
        private static TrashManager instance = null;
        
        private int actCount = 0;
        
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
