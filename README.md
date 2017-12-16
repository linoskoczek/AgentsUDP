# Agents with clock sync and Controller

_Author of the project: Marcin Węgłowski, s15237_

_**Requirements:**_ 
- local network
- Java Runtime Environment 8
- `java` as a location of java.exe file in Java Runtime Environment 8 directory

## Overview

This project was to create a network of Agents who synchronize their clock values. Agents send and receive UDP messages. For sending, they use broadcast address in a local network.
There is also a controller, which supports reading/writing clock & time period between synchronizations.

More details about what the project should do are available in the task itself.


## Settings
Before you start, you might want to change the default settings. You can find them under `src/Utilities/Settings`

```/* You can change those */
    public static final int agentPort = 10000;
    public static final int controllerPort = 15000;
    public static int timeToWaitForAnswers = 1;//in seconds
 ```

- **agentPort** - states for the default port of datagram socket of an Agent,
- **controllerPort** - states for the default port of a datagram socket of an Agent,
- **timeToWaitForAnswers** - time to wait for answers on a broadcasted request. The smaller the value is, the more precise calculations of counters are, but be aware, that too small value might lead to ignoring messages from Agents, which's messages were going a bit too long through the network.

## **Agent**

There can be only one agent working at the same time on one machine. This is because Agents have fixed port numbers and you cannot open two or more sockets with same port on one machine.

To start an Agent, you have to know the parameters that it takes:

1. initial counter value for this agent,
2. time period (in seconds), which denotes the interval between synchronization
procedure done by an agent,
3. broadcast address of the network that Agents are connecting to.

Don't panic if you don't remember it. If you run an Agent without parameters, it will remind you about it :)

### Example

In this example, we will run an Agent with initial counter = 0, time period = 5 and broadcast address = 192.168.1.255. 

```java Agent.Agent 0 5 192.168.1.255```

### How does it work?

An Agent will start and will immediately send broadcast message to receive counter values of other Agents. It will repeat it every **timeToWaitBetweenSync** second(s).
An Agent calculates the average of received counter values including it's own and then sets his clock to this calculated average.

_Please notice, that an Agent will send the clock value to itself and it's a conscious approach, because the delay of the clocks (due to it's transporting in the network) should be almost same for all Agents. I believe that this approach is very good for small, local networks._

An Agent can receive few types of messages:
- **CLK** - request to send back it's clock received using broadcasting by another agent
- **GCL** - request to send back it's clock received from Controller
- **GTP** - request to send back it's time period received from Controller
- **WCL** - request to change it's counter value to the one given in a request received from Controller
- **WTP** - same as above, but requests to change time period instead of counter

**WCL** and **WTP** send back acknowledge message with status _OK_ or _ERR_ depending on the fact whether the value was changes or not.  

## **Controller**

As with Agents, you have to have only one Controller turned on at the same time on one machine. It won't be a big problem though, because Controller runs no longer than **timeToWaitForAnswers**. Usually it's a matter of miliseconds. 

To start a Controller, you have to know the parameters that it takes.
#### If you want to _read_ a value

1. IP address of an Agent
2. keyword `get`,
3. either `counter` or `period`, depending on a value to be read.

#### If you want to _set_ a value

1. IP address of an Agent
2. keyword `set`,
3. either `counter` or `period`, depending on a value to be read,
4. new value.

Don’t panic if you don’t remember it. If you run an Controller without parameters, it will remind you about it :)

### Example of reading a value

We will read value of a counter of an Agent running at 192.168.1.123.

```java Controller.Controller 192.168.1.123 get counter```

### Example of setting a value

We will set value of a time period between synchronizations for an Agent running at 192.168.1.123 to 10 seconds.

```java Controller.Controller 192.168.1.123 set period 10```

### How does it work?

A Controller can send few types of messages:
- **CLK** - request to send back it's clock
- **GCL** - request to send back it's clock
- **GTP** - request to send back it's time period
- **WCL** - request to change it's counter value to the given one
- **WTP** - same as above, but requests to change time period instead of counter

A Controller will show the value it wanted to read or the result of a request to change a value: 
- _OK_ if everything went good,
- _ERR_ if the request was received, but counter or period cannot be changed to given value

Otherwise, an error will occur showing that it cannot connect to the given Agent or timeout will occur.

## *What was (or wasn't) implemented*

- [x] broadcast messaging between agents
- [x] clock synchronization in a loop (taking time period given as an argument into consideration)
- [x] changing clock to average of clocks of all Agents in the network
- [x] handling disconnect situations (used approach didn't require any special code changes for that)
- [x] changing and reading the values of counter and time period via Controller

## *Bugs and weaknesses*
- counter sync doesn't look at latency
- no protection against understandable command floods
- time in miliseconds stored in long variables - if Agents would have to work for longer time, consider changing miliseconds to smaller (in size of number) unit

_Project was created during process of education on PJATK._
