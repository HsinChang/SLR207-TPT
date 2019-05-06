# SLR207

## How to run

All four source files can be found in the **/src/main/java/** folder, in order to run it, a few preparations need to be done.

1. the folder to perform remotely is `/tmp/xizhang` 
2. modify the file **machines.txt** in the root folder with the machines you want to use. 
3. Then run the **DEPLOY**, it will print the deployment result to the terminal, you can choose several successful ones for the next step.
4. After than, we need to change the target machines to chosen machines in **MASTER** from line 27, just follow the pattern <br> `targetMachines.put("cxxx-xx", _index);`.
5. A compliled `slave.jar` file can already be found in the xizhang folder.



