package com.pedronveloso.sndinitdefy;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface to the Superuser shell on Android devices with some helper
 * functions.
 * <p/>
 * <p/>
 * Common usage for su shell:
 * <p/>
 * <code>if(ShellInterface.isSuAvailable()) { ShellInterface.runCommand("reboot"); }</code>
 * <p/>
 * <p/>
 * To get process output as a String:
 * <p/>
 * <code>if(ShellInterface.isSuAvailable()) { String date = ShellInterface.getProcessOutput("date"); }</code>
 * <p/>
 * <p/>
 * To run command with standard shell (no root permissions):
 * <code>ShellInterface.setShell("sh");</code>
 * <p/>
 * <code>ShellInterface.runCommand("date");</code>
 * <p/>
 * <p/>
 */
public class ShellInterface {
    private static final String TAG = Constants.LOG_TAG;

    private static String shell;

    // uid=0(root) gid=0(root)
    private static final Pattern UID_PATTERN = Pattern
            .compile("^uid=(\\d+).*?");

    enum OUTPUT {
        STDOUT, STDERR, BOTH
    }

    private static final String EXIT = "exit\n";

    private static final String[] SU_COMMANDS = new String[]{"su",
            "/system/xbin/su", "/system/bin/su"};

    private static final String[] TEST_COMMANDS = new String[]{"id",
            "/system/xbin/id", "/system/bin/id"};

    public static synchronized boolean isSuAvailable() {
        if (shell == null) {
            checkSu();
        }
        return shell != null;
    }

    public static synchronized void setShell(String shell) {
        ShellInterface.shell = shell;
    }

    private static boolean checkSu() {
        for (String command : SU_COMMANDS) {
            shell = command;
            if (isRootUid())
                return true;
        }
        shell = null;
        return false;
    }

    private static boolean isRootUid() {
        String out = null;
        for (String command : TEST_COMMANDS) {
            out = getProcessOutput(command);
            if (out != null && out.length() > 0)
                break;
        }
        if (out == null || out.length() == 0)
            return false;
        Matcher matcher = UID_PATTERN.matcher(out);
        if (matcher.matches()) {
            if ("0".equals(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    public static String getProcessOutput(String command) {
        try {
            return _runCommand(command, OUTPUT.STDERR);
        } catch (IOException ignored) {
            return null;
        }
    }

    public static boolean runCommand(String command) {
        try {
            _runCommand(command, OUTPUT.BOTH);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static String _runCommand(String command, OUTPUT o)
            throws IOException {
        DataOutputStream os = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(shell);
            os = new DataOutputStream(process.getOutputStream());
            InputStreamHandler sh = sinkProcessOutput(process, o);
            os.writeBytes(command + '\n');
            os.flush();
            os.writeBytes(EXIT);
            os.flush();
            process.waitFor();
            if (sh != null) {
                String output = sh.getOutput();
                Log.d(TAG, command + " output: " + output);
                return output;
            } else {
                return null;
            }
        } catch (Exception e) {
            final String msg = e.getMessage();
            Log.e(TAG, "runCommand error: " + msg);
            throw new IOException(msg);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static InputStreamHandler sinkProcessOutput(Process p, OUTPUT o) {
        InputStreamHandler output = null;
        switch (o) {
            case STDOUT:
                output = new InputStreamHandler(p.getErrorStream(), false);
                new InputStreamHandler(p.getInputStream(), true);
                break;
            case STDERR:
                output = new InputStreamHandler(p.getInputStream(), false);
                new InputStreamHandler(p.getErrorStream(), true);
                break;
            case BOTH:
                new InputStreamHandler(p.getInputStream(), true);
                new InputStreamHandler(p.getErrorStream(), true);
                break;
        }
        return output;
    }

    private static class InputStreamHandler extends Thread {
        private final InputStream stream;
        private final boolean sink;
        StringBuffer output;

        public String getOutput() {
            return output.toString();
        }

        InputStreamHandler(InputStream stream, boolean sink) {
            this.sink = sink;
            this.stream = stream;
            start();
        }

        @Override
        public void run() {
            try {
                if (sink) {
                    while (stream.read() != -1) {
                    }
                } else {
                    output = new StringBuffer();
                    BufferedReader b = new BufferedReader(
                            new InputStreamReader(stream));
                    String s;
                    while ((s = b.readLine()) != null) {
                        output.append(s);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    } // end of input stream handler


    //** USEFUL LOW LEVEL STUFF **//
    public static void turn_on_red_led(boolean state) {
        if (state) {
            ShellInterface.runCommand("echo \"0\" > /sys/class/leds/red/brightness");
            ShellInterface.runCommand("echo \"1000\" > /sys/class/leds/red/brightness");
        } else
            ShellInterface.runCommand("echo \"0\" > /sys/class/leds/red/brightness");
    }

    public static void turn_on_blue_led(boolean state) {
        if (state) {
            ShellInterface.runCommand("echo \"0\" > /sys/class/leds/blue/brightness");
            ShellInterface.runCommand("echo \"1000\" > /sys/class/leds/blue/brightness");
        } else
            ShellInterface.runCommand("echo \"0\" > /sys/class/leds/blue/brightness");
    }

    public static void turn_on_green_led(boolean state) {
        if (state) {
            ShellInterface.runCommand("echo \"0\" > /sys/class/leds/green/brightness");
            ShellInterface.runCommand("echo \"1000\" > /sys/class/leds/green/brightness");
        } else
            ShellInterface.runCommand("echo \"0\" > /sys/class/leds/green/brightness");
    }

    /**
	 * @return
	 * True if BusyBox was found.
	 */
	public static boolean busyboxAvailable() {
		Set<String> tmpSet = new HashSet<String>();
		//Try to read from the file.
        LineNumberReader lnr = null;
        try {
            File file = new File("/system/bin/busybox");
                    if (file.exists()) {
                        return true;
                    }
        } catch (Exception e) {
			Log.e(TAG, "BusyBox was not found.");
			e.printStackTrace();
        	return false;
        }
		return false;
	}

    /**
	 * @return
	 * True if BusyBox was found.
	 */
	public static boolean isBootmenuInstalled() {
		Set<String> tmpSet = new HashSet<String>();
        try {
            File file = new File("/system/bootmenu");
                    if (file.exists()) {
                        return true;
                    }
        } catch (Exception e) {
			e.printStackTrace();
        	return false;
        }
		return false;
	}


    /**
	 * @return
	 * True if BusyBox was found.
	 */
	public static boolean isBackupsFolderPresent() {
		Set<String> tmpSet = new HashSet<String>();
        try {
            File file = new File("/system/backups");
                    if (file.exists()) {
                        return true;
                    }
        } catch (Exception e) {
			e.printStackTrace();
        	return false;
        }
		return false;
	}

    /**
	 * @return
	 * True if BusyBox was found.
	 */
	public static boolean areBackupFilesPresent() {
		Set<String> tmpSet = new HashSet<String>();
        try {
            File file = new File("/system/backups/logwrapper");
                    if (file.exists()) {
                        file = new File("/system/backups/logwrapper.bin");
                        if (file.exists())
                        {
                            return true;
                        }
                    }
        } catch (Exception e) {
			e.printStackTrace();
        	return false;
        }
		return false;
	}


    public final static String SCRIPT_NAME = "surunner.sh";

	public static Process runSuCommandAsync(Context context, String command) throws IOException
	{
		DataOutputStream fout = new DataOutputStream(context.openFileOutput(SCRIPT_NAME, 0));
		fout.writeBytes(command);
		fout.close();

		String[] args = new String[] { "su", "-c", ". " + context.getFilesDir().getAbsolutePath() + "/" + SCRIPT_NAME };
		Process proc = Runtime.getRuntime().exec(args);
		return proc;
	}

	public static int runSuCommand(Context context, String command) throws IOException, InterruptedException
	{
		return runSuCommandAsync(context, command).waitFor();
	}

	public static int runSuCommandNoScriptWrapper(Context context, String command) throws IOException, InterruptedException
	{
		String[] args = new String[] { "su", "-c", command };
		Process proc = Runtime.getRuntime().exec(args);
		return proc.waitFor();
	}


}