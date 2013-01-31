using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CraftCraft.Test
{

public class Timer {
	private String name;
    private DateTime startTime;

	public Timer() {
	}

    public static void timeit(String name, int numRuns, System.Threading.ThreadStart r)
    {
		// Let the JIT work a bit
		for ( int i = 0; i < 1000; i++ ) {
            r.Invoke();
		}

		DateTime start = DateTime.Now;
		for ( int i = 0; i < numRuns; i++ ) {
            r.Invoke();
		}
		DateTime end = DateTime.Now;
        Console.WriteLine(name + ":" + (end - start).TotalMilliseconds);
	}

	public void start( String name ) {
		this.name = name;
        startTime = DateTime.Now; ;
	}

	public void stop() {
        DateTime end = DateTime.Now;
        Console.WriteLine(name + ":" + (end - startTime).TotalMilliseconds);
	}
}

}
