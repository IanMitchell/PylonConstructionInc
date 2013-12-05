package javabot.controllers;


import javabot.JavaBot;
import javabot.models.Unit;


public class TrashManager implements Manager {
        private static TrashManager instance = null;
        
        private int actCount = 0;
        private int msgCount = 40;
        
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
            
            messages[22] = "One time, I was walking in a park";
            messages[23] = "I saw some rabbits, some flowers, some dogs";
            messages[24] = "Eventually I came across a dirt path, the road less travelled by";
            messages[25] = "I was pretty bored at the time, so I went down this path";
            messages[26] = "It was pretty overgrown with blackberry bushes";
            messages[27] = "Being winter, there weren't any berries unfortunately";
            messages[28] = "As I kept walking, I found a small pond";
            messages[29] = "I was fascinated by it. I had no idea it was there";
            messages[30] = "It also had a very grimy, scummy look about it";
            messages[31] = "Not unlike that of your group member, Antonia.";
            
            messages[32] = "Antonia, there is no shame in surrendering, of course";
            messages[33] = "Go ahead. Just raise the white flag";
            messages[34] = "I'm sure no one would judge you.";
            messages[35] = "We're kinda what you would call professionals in our business";
            messages[36] = "What business you ask? We're in the industry of winning";
            messages[37] = "Oh look. Our Templars are TERRAN it up.";
            messages[38] = "Ok seriously I'm running out of things to say";
            messages[39] = "Can we just go play Age of Empires now? Please?";
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
                        	printMessage("gg scrubs");
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
