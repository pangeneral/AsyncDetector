/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Linjie Pan
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package cn.ac.ios.asyncdetect.constant;

public class AsyncClass {
	public final static String ASYNC_TASK="android.os.AsyncTask";
	public final static String INTENT_SERVICE="android.app.IntentService";
	public final static String HANDLER_THREAD="android.os.HandlerThread";
	public final static String ASYNC_TASK_LOADER="android.content.AsyncTaskLoader";
	public final static String ASYNC_QUERY_HANDLER="android.content.AsyncQueryHandler";
	public final static String THREAD_POOL_EXECUTOR="java.util.concurrent.ThreadPoolExecutor";
	public final static String THREAD="java.lang.Thread";
}
