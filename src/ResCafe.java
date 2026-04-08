/* $Header: /home/gbsmith/projects/MacResReader/ResCafe1.1/src/RCS/ResCafe.java,v 1.6 1999/10/28 04:01:33 gbsmith Exp $ */

import java.awt.Dialog;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.File;
import java.io.RandomAccessFile;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: ResCafe.java,v $
 * Revision 1.6  1999/10/28 04:01:33  gbsmith
 * Changed to support DocumentManager vs. one ResourceModel.
 *
 * Revision 1.5  1999/10/27 07:18:31  gbsmith
 * Put handler loading into a thread
 *
 * Revision 1.4  1999/10/21 23:08:54  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.3  1999/10/19 03:56:30  gbsmith
 * Added accented e to title. Moved window slightly.
 *
 * Revision 1.2  1999/10/13 07:56:26  gbsmith
 * Altered load call to pass a 'File' rather than a
 * 'String' filename
 *
 * Revision 1.1  1999/10/04 21:24:02  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class ResCafe
{
   /*--- Data -----------------------------------------------------------*/
   jMainResourceView myview;
   DocumentManager   mydocmgr;
   HandlerTable      myhandlers;
   FileController    myfctrl;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ResCafe.java,v 1.6 1999/10/28 04:01:33 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public static void main( String args[] )
   {
      ResCafe app = new ResCafe();

      // Load a file if given a name
      if(args.length > 0) app.mydocmgr.load(new File(args[0]));
   }

   /*--------------------------------------------------------------------*/
   public ResCafe()
   {
      mydocmgr   = new DocumentManager();
      myfctrl    = new FileController();
      myhandlers = new HandlerTable();

      // Load handlers in own thread
      Thread handlerThread = new Thread(myhandlers);
      handlerThread.start();

      // Attach parts to View
      myview  = new jMainResourceView("ResCafé Resource Extractor");
      myview.setDocManager(mydocmgr);
      myview.setHandlers(myhandlers);
      myview.setFileController(myfctrl);

      // Attach parts to Controller
      myfctrl.setView(myview);
      myfctrl.setDocManager(mydocmgr);

      // Attach parts to Doc Manager
      mydocmgr.setHandlers(myhandlers);

      // Show the view... and begin
      myview.setLocation( 75, 75 );
      myview.setSize( 800, 500 );
      myview.show();
   }
}
