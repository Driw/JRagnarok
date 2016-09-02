package org.diverproject.jragnarok.server;

import static org.diverproject.jragnarok.JRagnarokUtil.free;
import static org.diverproject.log.LogSystem.logError;
import static org.diverproject.log.LogSystem.logWarning;

import org.diverproject.util.TimerTick;
import org.diverproject.util.collection.Map;
import org.diverproject.util.collection.Map.MapItem;
import org.diverproject.util.collection.abstraction.IntegerLittleMap;
import org.diverproject.util.collection.abstraction.StringSimpleMap;
import org.diverproject.util.lang.LongUtil;

public class TimerSystem
{
	public static final int MIN_TIMER_INTERVAL = 20;
	public static final int MAX_TIMER_INTERVAL = 1000;

	private static final TimerSystem INSTANCE = new TimerSystem();

	private long started;
	private long lastTick;
	private TimerTick tick;
	private Map<Integer, Timer> timers;
	private Map<String, TimerListener> listeners;

	private TimerSystem()
	{
		timers = new IntegerLittleMap<>();
		listeners = new StringSimpleMap<>();
	}

	public void addListener(TimerListener listener, String name)
	{
		if (listener != null && name != null)
		{
			if (listeners.containsKey(name))
				logWarning("função duplicada (name: %s)", name);
			else
				listeners.add(name, listener);
		}
	}

	public String searchListener(TimerListener listener)
	{
		for (MapItem<String, TimerListener> item : listeners.iterateItems())
			if (item.value.equals(listener))
				return item.key;

		return "timer listener desconhecido";
	}

	public Timer acquireTimer()
	{
		Timer timer = new Timer();
		timers.add(timer.getID(), timer);

		return timer;
	}

	public Timer addTimer(long tick, TimerListener listener, int id, int data)
	{
		Timer timer = timers.get(id);

		if (timer == null)
		{
			timer = acquireTimer();
			timers.remove(timer);
		}

		timer.setID(id);
		timer.setTick(tick);
		timer.setData(data);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);
		timer.setInterval(1000);

		return timer;
	}

	public Timer addTimerInterval(long tick, TimerListener listener, int id, int data, int interval)
	{
		if (interval < 1)
		{
			logError("intervalo inválido (tick: %d, listener: %s, id: %d, data: %d).\n", tick, searchListener(listener), id, data);
			return null;
		}

		Timer timer = timers.get(id);

		if (timer == null)
		{
			timer = acquireTimer();
			timers.remove(timer);
		}

		timer.setID(id);
		timer.setTick(tick);
		timer.setListener(listener);
		timer.setData(data);
		timer.getType().set(Timer.TIMER_INTERVAL);
		timer.setInterval(interval);

		return timer;
	}

	public Timer getTimer(int tid)
	{
		return timers.get(tid);
	}

	public TimerDeleteResult deleteTimer(Timer timer, TimerListener listener)
	{
		if (timers.get(timer.getID()) == null)
			return TimerDeleteResult.INDEX;

		if (!timer.getListener().equals(listener))
			return TimerDeleteResult.LISTENER;

		timer.setListener(null);
		timer.getType().set(Timer.TIMER_ONCE_AUTODEL);

		return TimerDeleteResult.SUCCESSFUL;
	}

	public long addTickTimer(Timer timer, long tick)
	{
		return setTickTimer(timer, timer.getTick() + tick);
	}

	private long setTickTimer(Timer timer, long tick)
	{
		if (tick == -1)
			tick = 0;

		if (timer.getTick() == tick)
			return tick;

		timer.setTick(tick);

		return tick;
	}

	public long tick()
	{
		return (lastTick = tick.getTicks());
	}

	public long getLastTick()
	{
		return lastTick;
	}

	public long update(long tick)
	{
		long diff = MAX_TIMER_INTERVAL;

		for (Timer timer : timers)
		{
			diff = timer.getTick() - tick;

			if (diff > 0)
				break;

			timer.getType().set(Timer.TIMER_REMOVE);

			if (timer.getListener() != null)
			{
				if (diff < -1000)
					timer.getListener().onCall(timer, tick);
				else
					timer.getListener().onCall(timer, timer.getTick());
			}

			if (timer.getType().is(Timer.TIMER_REMOVE))
			{
				timer.getType().unset(Timer.TIMER_REMOVE);

				switch (timer.getType().getValue())
				{
					default:
					case Timer.TIMER_ONCE_AUTODEL:
						timer.getType().setValue(0);
						timers.remove(timer);
						break;

					case Timer.TIMER_INTERVAL:
						if (timer.getTick() - tick < -1000)
							timer.setTick(tick + timer.getInterval());
						else
							timer.setTick(timer.getTick() + timer.getInterval());
						break;
				}
			}
		}

		return LongUtil.limit(diff, MIN_TIMER_INTERVAL, MAX_TIMER_INTERVAL);
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - started;
	}

	public void init()
	{
		started = System.currentTimeMillis();
		tick = new TimerTick(1, Long.MAX_VALUE);
	}

	public void terminate()
	{
		timers.clear();

		free();
	}

	public static TimerSystem getInstance()
	{
		return INSTANCE;
	}
}
