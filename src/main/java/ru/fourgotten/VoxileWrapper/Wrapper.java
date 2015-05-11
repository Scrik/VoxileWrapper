package ru.fourgotten.VoxileWrapper;
import ru.fourgotten.VoxileSecurity.Data.MessagesWrapper.MessageWrappedGame;
import ru.fourgotten.VoxileSecurity.SecureConnection;
import ru.fourgotten.VoxileSecurity.SecureConnection.ConnectionEstablishedNotification;
import ru.fourgotten.VoxileSecurity.WrappedGameStarter;

public final class Wrapper extends WrappedGameStarter implements ConnectionEstablishedNotification
{
	private static final int timeoutMSec = 1000 * 30;
	private static final int granuleMSec = 1000 * 2;
	public MessageWrappedGame receiveMessageFromLauncher(int port)
	{
		try
		{
			final SecureConnection connection = new SecureConnection(null, port, this);
			connection.start();
			System.out.println("Connecting to the launcher (" + port + ")...");
			for(int interval = 0; !Thread.interrupted() && (interval * granuleMSec < timeoutMSec); Thread.sleep(granuleMSec), interval += 1)
			{
				if(connection.isConnected())
				{
					connection.getWrapper().writeLine("wrapper2launcher");
					return connection.getWrapper().readObject(MessageWrappedGame.class);
				}
				System.out.print(".");
			}
		} catch(InterruptedException ex) {
			System.err.println(ex);
		} catch(RuntimeException ex) {
			System.err.println(ex);
		}
		return null;
	}
	@Override
	public void connectionEstablished(SecureConnection connection)
	{
		System.out.println("\nConnection with launcher has been established!");
	}
	@Override
	public void connectionBreaked()
	{
	}
	private void run(String[] args)
	{
		// Test for right code location
		final String myOwnPath = myOwnURL.getPath().toLowerCase();
		if(!myOwnPath.endsWith(".jar") && !myOwnPath.endsWith(".exe"))
		{
			System.err.println("Bad source location! (1)");
			System.exit(1);
		}
		// Connect and receive starting parameters
		if(args.length != 2)
		{
			System.err.println("Wrong command line! (2)");
			System.exit(2);
		}
		if(!"--port".equalsIgnoreCase(args[0]))
		{
			System.err.println("Wrong command line! (3-1)");
			System.exit(3);
		}
		try
		{
			final int localLauncherPort = Integer.parseInt(args[1]);
			final MessageWrappedGame msg = receiveMessageFromLauncher(localLauncherPort);
			if(msg == null)
			{
				System.err.println("Cannot receive data from local launcher instance!\n"
					+ "Please check if the firewall is blocking it.");
				System.exit(4);
			}
			msg.tweakerClass = Tweaker.class.getCanonicalName();
			System.exit(startGameInCurrentProcess(msg));
		} catch(NumberFormatException ex) {
			System.err.println("Wrong command line! (3-2)");
			System.exit(3);
		}
	}
	public static void main(String[] args)
	{
		final Wrapper wrapper = new Wrapper();
		instance = wrapper;
		wrapper.run(args);
	}
}