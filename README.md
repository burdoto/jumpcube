# jumpcube
Continuation of the DeathCube plugin

This minigame is a JumpNRun game, where you spawn into a large, randomly generated block of wool with pumpkins. The lanterns can be mined and placed, and you get to keep any lantern that you did not use in the end. The goal is to be the first to reach the top of the cube.

## How to set up a cube

1. Create the cube by using `/jumpcube create <NAME>` where you replace "<NAME>" with the name of the cube.
  
  2. Paste the BlockBar somewhere _OUTSIDE_ from the cube area. This is done by doing `/jumpcube bar`. The bar will extend 4 blocks into the positive X direction from the player location.
  3. Define the corner positions for the cube. The command for this is: `/jumpcube pos <1/2>`. The recommended size for your area is 48x48.
  4. Complete cube setup with `/jumpcube confirm`. _Note: If the cube cannot be used after this command, a server restart will make the cube usable. This is a known bug._

## Permissions

`jumpcube.user` -> All required commands for playing JumpCube

`jumpcube.vip.earlystart` -> Access to `/jumpcube start`

`jumpcube.vip.bringplaceable` -> The placeable block will not be removed from your inventory when you join the game

`jumpcube.mod.teleport` -> Access to teleporting out of the cube during a game (Currently unused)

`jumpcube.mod.regenerate` -> Access to regenerating the cube manually with `/jumpcube regenerate` or `/jumpcube regenerate-complete`

`jumpcube.admin` -> Access to commands used for creating cubes

`jumpcube.admin.debug` -> Recieve debug information in ingame-chat
