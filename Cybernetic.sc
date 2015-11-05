/* Cybernetic.sc - various SuperCollider tricks for the Cybernetic Orchestra

Examples of usage:
Cybernetic.put(\scale,[0,2,3,5,7]); // set everyone's ~scale to [0,2,3,5,7]
Cybernetic.do( { \hello.postln;} ); // evaluate some code on everyone's machine
Cybernetic.allowPut = false; // stop others from setting your environment variables
Cybernetic.allowDo = false; // stop others from running code on your machine
Cybernetic.allowCodeShare = false; // stop your code evals from being shared

Notes:
-for this to work, the Esp class must be installed (Esp.sc) and EspGrid must be running
*/


Cybernetic {

	classvar <version;
	classvar <>allowPut;
	classvar <>allowDo;
	classvar <>allowCodeShare;
	classvar <>directBroadcast; // set to true to bypass EspGrid
	classvar <broadcast; // set to something else if default 255.255.255.255 not applicable

	classvar send;

	*initClass {
		super.initClass;
		version = "5 November 2015";
		allowPut = false;
		allowDo = false;
		allowCodeShare = true;
		directBroadcast = false;
		NetAddr.broadcastFlag = true;
		Cybernetic.broadcast_("255.255.255.255"); // also initializes 'send' object
		("Cybernetic.sc: " ++ version).postln;

		StartUp.add {

			OSCdef(\put,
				{ |msg,time,addr,port|
					if(allowPut,{
						("/cybernetic/put " ++ msg[1].asString ++ " " ++ msg[2].asString).postln;
						topEnvironment.put(msg[1],msg[2].asString.compile.value);});
			},"/cybernetic/put").permanent_(true);

			OSCdef(\do,
				{ |msg,time,addr,port|
					if(allowDo,{
						("/cybernetic/do " ++ msg[1].asString).postln;
						msg[1].asString.interpret.value;
					});
			},"/cybernetic/do").permanent_(true);

			OSCdef(\pbindef,
				{ |msg,time,addr,port|
					if(allowDo,{
						("/cybernetic/pbindef " ++ msg[1].asString ++ " " ++ msg[2].asString ++ " " ++ msg[3].asString).postln;
						Pbindef(msg[1].asSymbol,msg[2].asSymbol,msg[3].asString.interpret.value);
					});
			},"/cybernetic/pbindef").permanent_(true);

			thisProcess.interpreter.codeDump = {
				|code|
				if(allowCodeShare && (directBroadcast==false),
					{
						Esp.send.sendMsg("/esp/codeShare/post","SuperCollider",code);
				});
			};
		};
	}

	*broadcast_ {
		|x|
		broadcast = x;
		send = NetAddr(broadcast,NetAddr.langPort);
	}

	*put { // broadcast a change to everyone's top environment variables
		| key,value |
		if(directBroadcast,{
			send.sendMsg("/cybernetic/put",key,value.asCompileString);
		},{
			Esp.send.sendMsg("/esp/msg/now","/cybernetic/put",key,value.asCompileString);
		});
	}

	*do { // broadcast code to evaluate on everyone's machine
		|code|
		if(directBroadcast,{
			send.sendMsg("/cybernetic/do",code.asCompileString);
		},{
			Esp.send.sendMsg("/esp/msg/now","/cybernetic/do",code.asCompileString);
		});
	}

	*pbindef {
		| key1,key2,value |
		if(directBroadcast,{
			send.sendMsg("/cybernetic/pbindef",key1,key2,value.asCompileString);
		},{
			Esp.send.sendMsg("/esp/msg/now","/cybernetic/pbindef",key1,key2,value.asCompileString);
		});
	}
}
