/*
 * This file is part of Domodroid.
 *
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 *
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package Abstract;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class common_method {
    /**
     * This method simply refresh the current view
     *
     * @param widgetHandler an Handler used to callback
     */
    public static void refresh_the_views(Handler widgetHandler) {
        //Refresh the view
        Bundle b = new Bundle();
        b.putBoolean("refresh", true);
        Message msg = new Message();
        msg.setData(b);
        widgetHandler.sendMessage(msg);
    }


}
