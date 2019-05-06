# SLR207

## How to run

All four source files can be found in the **/src/main/java/** folder, in order to run it, a few preparations need to be done.

1. the folder to perform remotely is `/tmp/xizhang` 
2. modify the file **machines.txt** in the root folder with the machines you want to use. 
3. Then run the **DEPLOY**, it will print the deployment result to the terminal, you can choose several successful ones for the next step.
4. After than, we need to change the target machines to chosen machines in **MASTER** from line 27, just follow the pattern `targetMachines.put("cxxx-xx", _index);`.
5. A compliled `slave.jar` file can already be found in the xizhang folder.
6. The **WordCount** file is the local word counter for the first few questions.


## Some answers of the questions

### Q5
first 5 words in *deontologie_police_nationale.txt*

* de 86 
* la 40 
* police 29 
* et 27 
* à 25 

### Q6
first 5 words in *domaine_public_fluvial.txt*

* de 621
* le 373 
* du 347
* la 330
* et 266

### Q7 & Q8
first 5 words in *sante_publique.txt*

* hasard 2 
* haut-parleur. 2 
* haut-parleurs 2 
* hautes) 2
* hebdomadaires.Lorsque 2

Time for the counting 482ms 

Time for the sorting: 109ms 


### Q9
CC-MAIN-20170322212949-00140-ip-10-233-31-227.ec2.internal.warc.wet

Time for the counting: 20536ms

Time for the sorting: 7058ms

### Q10
```
hostname -a
hostname -A
```

### Q11
```
hostname -I
```

### Q12
```
getent hosts c45-12.enst.fr
```

### Q13
```
host 137.194.34.215
```

### Q14 & Q15
All the three methods work.

### Q16
```
echo $((2+3))
```

### Q17 & Q18
```
ssh c45-21
ssh xizhang@c45-12.enst.fr
```
### Q19
```
cd
pwd
// cal/homes/xizhang
```
### Q27
```
scp local.txt xizhang@c45-12:/tmp
```
### Q28
```
scp xizhang@c45-12:/tmp/xizhang/local.txt xizhang@c45-21:/tmp
```
### Q32
```
ssh xizhang@c45-12 java -jar /tmp/xizhang/slave.jar
```
