/**
 Yet Another/Android SU Library.

 <p>
 The Yasul library is a factory of SU shell sessions, that permit to execute <i>command strings</i>
 as a privileged user, or <i>super user</i>. This requires a <i>rooted</i> device with
 a suitable <code>su</code> program installed.
 Examples are:
 <ul>
 <li><a href="https://play.google.com/store/apps/details?id=eu.chainfire.supersu">SuperSU</a>: it
 is widely spread, and Yasul is tested upon it
 </li>
 <li><a href="https://play.google.com/store/apps/details?id=com.noshufou.android.su">Superuser</a>:
 it's also commonly used, and should work just fine
 </li>
 </ul>
 </p>

 <p>
 For the long story <a href="http://su.chainfire.eu/">Chainfire's How-To SU</a> is definitely worth
 reading, but for short:
 <ul>
 <li>command strings like <code>ps | grep phone | cut -f1 -d ' '</code> may prove painful to
 execute reliably through options provided to disparate <code>su</code> programs
 </li>
 <li>it's <i>better</i> (see later) to rely upon long living shell sessions rather than to
 fork a new shell process at each command string execution
 </li>
 </ul>
 To achieve this, the idea is to emulate an interactive session, a <i>TTY</i>, that reads from and
 writes to the shell process standard file descriptors <code>STDIN</code>, <code>STDOUT</code>,
 and <code>STDERR</code>. Yasul uses native local sockets as its IPC channel, and native threads
 and synchronization to process I/O streams. A {@link org.openmarl.yasul.Libyasul JNI bridge}
 interfaces with the managed Java code.
 </p>

 <p>
 To create a <i>root shell</i> typically involves to fork a process, and possibly go through some
 user interaction such as an <i>accept/refuse/timeout</i> confirmation dialog, which may be a long
 running operation, and as such should not happen on the main/UI thread of an Android application.
 Using sessions avoid to repeat unneededly this initialization stage.
 </p>

 <p>
 The user guide for this API is pretty simple:
 <ul>
 <li>there's a singleton {@link org.openmarl.yasul.YslContext context} that acts as a session
 factory
 </li>
 <li>a shell session creation is initiated through the
 {@link org.openmarl.yasul.YslContext#openSession(YslObserver, int, String) open()} asynchronous call,
 that is designed to permittedly occur on the application's main thread
 </li>
 <li>after successful initialization on a background thread, the client is signaled on the main thread,
 and should cache the acquired {@link org.openmarl.yasul.YslSession session}
 </li>
 <li>a valid session, which behavior is configured through control flags, provides privileged
 command string and batch execution, both synchronously and asynchronously
 </li>
 <li>if the shell process dies abnormally, a <i>borken pipe</i> occurs, and the session's is
 marked invalidated: any further call to the {@link org.openmarl.yasul.YslSession} and
 {@link org.openmarl.yasul.YslShell} API will fail with an
 {@link org.openmarl.yasul.YslEpipeException} error
 </li>
 <li>when the session is not needed anymore, one should completely release its associated resources
 through {@link org.openmarl.yasul.YslSession#exit(long, boolean) exit()}
 </li>
 </ul>
 </p>

 <p>
 Tasks most commonly involved during pre/post installation steps, or to execute binaries in a
 customized environment, will benefit from convenient wrappers provided by
 {@link org.openmarl.yasul.YslShell}.
 </p>

 <p>
 In some circumstances one would require that the shell process lives within a specific SE Linux
 context (other than <code>u:r:init:s0</code> or <code>u:r:init_shell:s0</code>).
 <a href="http://su.chainfire.eu/#selinux">SuperSU versions 1.90 and up</a> supports an option
 to specify a context switch: this feature is also accessible through
 {@link org.openmarl.yasul.YslContext#openSession(YslObserver, int, String) open()}
 when available.
 </p>

 <p>License: This code is available as under the GNU LGPLv2 and GPLv3 licenses.
 </p>

 <pre>
 t0kt0ckus@gmail.com
 (C) 2014

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version. You can also redistribute it
 and/or modify it under the terms of the GNU Library General Public
 License, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 </pre>

 */
package org.openmarl.yasul;