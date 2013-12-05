package javabot.controllers;


import javabot.JavaBot;
import javabot.models.Unit;


public class TrashManager implements Manager {
        private static TrashManager instance = null;
        
        private int actCount = 0;
        private int msgCount = 30;
        
        private String[] messages = new String[msgCount];

        
        private TrashManager() {
            messages[0] = "Sup Nick.";
            messages[1] = "Ready for some embarassment?";
            messages[2] = "We're gonna send you to the stone age.";
            messages[3] = "Yo Mama so fat she can turn a Creep Colony into a Sunken Colony.";
            messages[4] = "Go step on a lego.";
            messages[5] = "Ben, just know that...";
            messages[6] = "We're no strangers to love";
            messages[7] = "You know the rules and so do I";
            messages[8] = "A full commitment's what I'm thinking of";
            messages[9] = "You wouldn't get this from any other guy";
            messages[10] = "I just want to tell you how I'm feeling";
            messages[11] = "Gotta make you understand";
            messages[12] = "Never gonna give you up";
            messages[13] = "Never gonna let you down";
            messages[14] = "Never gonna run around and desert you";
            messages[15] = "Never gonna make you cry";
            messages[16] = "Never gonna say goodbye";
            messages[17] = "Never gonna tell a lie and hurt you";
            messages[18] = "lol jk we're gonna crush you";
            messages[19] = "Just doin our job, takin out the trash";
            messages[20] = "Good thing your marines have life insurance";
            messages[21] = "#thanksobama";
            messages[22] = "Antonia, there is no shame in surrendering, of course";
            messages[23] = "Go ahead. Just raise the white flag";
            messages[24] = "I'm sure no one would judge you.";
            messages[25] = "We're kinda what you would call professionals in our business";
            messages[26] = "What business you ask? We're in the industry of winning";
            messages[27] = "Oh look. Our Templars are TERRAN it up.";
            messages[28] = "Ok seriously I'm running out of things to say";
            messages[29] = "Can we just go play Age of Empires now? Please?";
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
                        int msg = (actCount / 5) - 1;
                        if (msg < msgCount) {
                                printMessage(messages[msg]);
                        }
                        else {
                        	actCount = 0;
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
       		JavaBot.bwapi.sendText(msg);
        }

		@Override
		public void reset() {
			// TODO Auto-generated method stub
		}


}
