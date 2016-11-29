drunken-ninja
=============

a simple price-time priority limit order book exchange simulator 

Import the maven project into eclipse / your favorite IDE and run test cases. 

The solution is consist of 2 main components, namely 
	* LimitOrderBook (LOB) - a order matching mechanism that produce Trade callback when a match happen, and as user you can peek into order book by getting a quote snapshot 
	* TickDataConvertor - a class that converts tick data in specified format into Limit Orders which can be handled by limit order book. 

To see the solution working in action, please look at TestSimulation test case, which has covered 1 scenario where tick data is loaded into LOB, and with a client sending an order, and how the trade event is delivered, and how the LOB change as a result of this client trade.

This simulator is designed with queue priority in mind, to keep the solution simple currently it will cancel+new order if tick data changes, but this behaviour could change to cater for better simulation of queue priority. 

