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

	*initClass {
		super.initClass;
		version = "8 October 2015";
		allowPut = false;
		allowDo = false;
		allowCodeShare = true;
		("Cybernetic.sc: " ++ version).postln;

		StartUp.add {

			OSCdef(\put,
				{ |msg,time,addr,port|
					if(allowPut,{topEnvironment.put(msg[1],msg[2].asString.compile.value);});
			},"/cybernetic/put").permanent_(true);

			OSCdef(\do,
				{ |msg,time,addr,port|
					if(allowDo,
						{
							msg[1].postln;
							msg[1].asString.interpret.value;
					});
			},"/cybernetic/do").permanent_(true);

			OSCdef(\pbindef,
				{ |msg,time,addr,port|
					if(allowDo,
						{
							msg[1].postln;
							Pbindef(msg[1].asSymbol,msg[2].asSymbol,
								msg[3].asString.interpret.value);
					});
			},"/cybernetic/pbindef").permanent_(true);

			thisProcess.interpreter.codeDump = {
				|code|
				if(allowCodeShare,
					{
						Esp.send.sendMsg("/esp/msg/now","/codescrolld","unknown",code);
						Esp.send.sendMsg("/esp/codeShare/post","SuperCollider",code);
				});
				code;
			};
		};
	}

	*put { // broadcast a change to everyone's top environment variables
		| key,value |
		Esp.send.sendMsg("/esp/msg/now","/cybernetic/put",key,value.asCompileString);
		^value;
	}

	*do { // broadcast code to evaluate on everyone's machine
		|code|
		Esp.send.sendMsg("/esp/msg/now","/cybernetic/do",code.asCompileString);
	}

	*pbindef {
		| key1,key2,value |
		Esp.send.sendMsg("/esp/msg/now","/cybernetic/pbindef",key1,key2,value.asCompileString);
	}
}
