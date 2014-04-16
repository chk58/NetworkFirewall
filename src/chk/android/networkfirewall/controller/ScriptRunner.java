package chk.android.networkfirewall.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.util.Log;
import chk.android.networkfirewall.MyApplication;
import chk.android.networkfirewall.Utils;

public class ScriptRunner extends Thread {

    // public interface ScriptListener {
    // public void onStarted();
    //
    // public void onSucceeded(String result);
    //
    // public void onFailed(String error);
    // }

    // private final String mScript;
    // private final long mTimeout;
    // private final File mCacheFile;
    // private final Handler mHandler;
    // private InternalRunner mRunner;
    //
    // public ScriptRunner(File file, String script) {
    // this(file, script, 0, null);
    // }

    // public ScriptRunner(File file, String script, ScriptListener l) {
    // this(file, script, 0, l);
    // }
    //
    // public ScriptRunner(File file, String script, long timeout,
    // ScriptListener l) {
    // mScript = script;
    // mTimeout = timeout;
    // mCacheFile = file;
    // mHandler = new MainHandler(l);
    // }
    //
    // @Override
    // public void run() {
    // String execResult = null;
    // String execError = null;
    // int exitValue = 1;
    // try {
    // mRunner = new InternalRunner();
    // mRunner.start();
    //
    // if (mTimeout > 0) {
    // mRunner.join(mTimeout);
    // } else {
    // mRunner.join();
    // }
    //
    // if (mRunner.isAlive()) {
    // // Timed-out
    // mRunner.interrupt();
    // execError = "Timed-out";
    // } else {
    // execResult = mRunner.mExecResult;
    // execError = mRunner.mExecError;
    // exitValue = mRunner.mExitValue;
    // }
    //
    // } catch (InterruptedException e) {
    // execError = e.toString();
    // } finally {
    // Message.obtain(mHandler, MainHandler.WHAT_FINISHED, exitValue, -1,
    // new String[] { execResult, execError }).sendToTarget();
    // }
    // }
    //
    // @Override
    // public synchronized void start() {
    // Message.obtain(mHandler, MainHandler.WHAT_STARTED).sendToTarget();
    // super.start();
    // }
    //
    // @Override
    // public void interrupt() {
    // if (mRunner != null) {
    // mRunner.interrupt();
    // mRunner = null;
    // }
    // super.interrupt();
    // }
    //
    // private static class MainHandler extends Handler {
    // private static final int WHAT_STARTED = 1;
    // private static final int WHAT_FINISHED = 2;
    // private ScriptListener mListener;
    //
    // public MainHandler(ScriptListener l) {
    // super(Looper.getMainLooper());
    // mListener = l;
    // }
    //
    // @Override
    // public void handleMessage(Message msg) {
    // if (mListener != null) {
    // switch (msg.what) {
    // case WHAT_STARTED:
    // mListener.onStarted();
    // break;
    // case WHAT_FINISHED:
    // String[] r = (String[]) msg.obj;
    // if (!TextUtils.isEmpty(r[1])) {
    // mListener.onFailed(r[1]);
    // } else {
    // mListener.onSucceeded(r[0]);
    // }
    // break;
    // }
    // }
    // }
    // }
    //
    // private class InternalRunner extends Thread {
    // private String mExecResult;
    // private String mExecError;
    // private int mExitValue = -1;
    //
    // @Override
    // public void run() {
    // String[] r = new String[2];
    // mExitValue = new ScriptCommand(mCacheFile, mScript, r).run();
    // mExecResult = r[0];
    // mExecError = r[1];
    // }
    // }

    // public static void runOnSameThread(File file, String script) {
    // runOnSameThread(file, script, null);
    // }

    public static int runOnSameThread(File file, String script,
            String[] resultInfo) {
        return new ScriptCommand(file, script, resultInfo).run();
    }

    private static class ScriptCommand {
        private final static byte[] sLock = new byte[0];
        private final File mFile;
        private final String mScript;
        private final String[] mResults;

        public ScriptCommand(File file, String script, String[] results) {
            mFile = file;
            mScript = script;
            mResults = results;
        }

        private static String getMessage(Throwable t) {
            StringBuilder sb = new StringBuilder();

            Throwable cause = t;
            while (cause != null) {
                sb.append(t.toString() + "/n");
                cause = cause.getCause();
            }

            return sb.toString();
        }

        public int run() {
            synchronized (sLock) {
                BufferedReader r = null;
                BufferedReader e = null;
                OutputStreamWriter fileOut = null;
                Process p = null;
                int execCode = -1;
                try {
                    final String abspath = mFile.getAbsolutePath();
                    Runtime.getRuntime().exec("chmod 700 " + abspath).waitFor();
                    fileOut = new OutputStreamWriter(
                            new FileOutputStream(mFile));
                    fileOut.write(mScript);
                    if (!mScript.endsWith("\n"))
                        fileOut.write("\n");
                    fileOut.write("exit\n");
                    fileOut.flush();
                    fileOut.close();

                    if (MyApplication.sAsRoot) {
                        p = Runtime.getRuntime().exec("su -c " + abspath);
                    } else {
                        p = Runtime.getRuntime().exec("sh " + abspath);
                    }

                    if (mResults != null && mResults.length == 2) {
                        String s;
                        StringBuilder execResult = new StringBuilder();
                        StringBuilder execError = new StringBuilder();
                        r = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
                        while ((s = r.readLine()) != null) {
                            execResult.append(s + "\n");
                        }

                        e = new BufferedReader(new InputStreamReader(
                                p.getErrorStream()));
                        while ((s = e.readLine()) != null) {
                            execError.append(s + "\n");
                        }
                        mResults[0] = execResult.toString();
                        String error = execError.toString();
                        if (error.contains("Permission denied")) {
                            mResults[1] = error;
                        }
                    }
                    execCode = p.waitFor();
                } catch (IOException e1) {
                    Log.e(Utils.TAG, e1.toString());
                    mResults[1] = getMessage(e1);
                } catch (InterruptedException e2) {
                    throw new RuntimeException(e2);
                } finally {
                    if (p != null) {
                        p.destroy();
                        p = null;
                    }

                    if (r != null) {
                        try {
                            r.close();
                        } catch (IOException ioe) {
                        }
                        r = null;
                    }

                    if (e != null) {
                        try {
                            e.close();
                        } catch (IOException ioe) {
                        }
                        e = null;
                    }

                    if (fileOut != null) {
                        try {
                            fileOut.close();
                        } catch (IOException ioe) {
                        }
                        fileOut = null;
                    }
                }
                return execCode;
            }
        }
    }
}
