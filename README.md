# Repository for Pylon Construction Inc



### Resources
Initial Resources / Build Order.

1. Ian
2. Garrett


*Note: For initial Build...*

* 8 pylon 
* 10 gateway (Around here, be ready to relinquish control of one worker to Scout)
* 12 gateway
* Zealot
* 16 assimilator


### Unit Creation
Army creation balanced with Building creation (Mid-game / late-game oriented, different from initial start) (Doesn't focus on why it's building it, but just does it)

1. Talha
2. James

### Army
Army movement / attacking

1. Alvin
2. Talha

### Scouting
Early scouting (change build orders as needed depending what is scouted)

1. Garrett
2. Ian

### Strategy 
Takes info from Scouting, alters desired builds for units and buildings. "Mastermind," tells when to attack and when to defend.

1. James
2. Alvin

### Trash Talk:

1. Ian
2. Garrett


## Code Structure

For version 1, we will want the following capabilities:

**Game Manager:**
 
 * Request Units
 * Decide who gets the unit
 * Handle Returned Units
 * Queue Buildings
 * Request Supply

**Resource Manager:**

 * Gather Minerals
 * Gather Gas
 * Determine when to assign to Gas

**Unit Manager:**

 [ ] Train Units
 * Watch Supply, alert GameManager when low
 * Give Units to GameManager

**Scout Manager:**

 * Scout
 * If it finds X, alert GameManager

**Build Manager:**

 * Build buildings
 * Choose building locations
 * Alert GameManager when finished building

**ArmyManager**

 * Track Units