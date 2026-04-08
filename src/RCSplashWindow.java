/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.3/src/RCS/RCSplashWindow.java,v 1.1 2000/11/27 19:47:29 gbsmith Exp $ */

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Observable;
import java.util.Observer;

/*=========================================================================*/
/* Copyright (c) 2000 by G. Brannon Smith -- All Rights Reserved           */
/*=========================================================================*/

/*=========================================================================*/
/*
 * $Log: RCSplashWindow.java,v $
 * Revision 1.1  2000/11/27 19:47:29  gbsmith
 * Initial revision
 *
 *
 */

/*=========================================================================*/
class RCSplashWindow extends JWindow implements Observer
{
   /*--- Data -------------------------------------------------------------*/
   JLabel imglab;
   JLabel txtlab;

   /*------ RCS -----------------------------------------------------------*/
   static final String rcsid = "$Id: RCSplashWindow.java,v 1.1 2000/11/27 19:47:29 gbsmith Exp $";

   /*--- Methods ----------------------------------------------------------*/
   public RCSplashWindow(String filename, Frame f)
   {
      super(f);
      Container myContentPane = getContentPane();

      imglab = new JLabel(new ImageIcon(filename));
      txtlab = new JLabel("Preparing ResCaf\u00e9..."); // supposed to be ResCafé
      myContentPane.add(imglab, BorderLayout.CENTER);
      myContentPane.add(txtlab, BorderLayout.SOUTH);
      pack();

      Dimension screenDim =
         Toolkit.getDefaultToolkit().getScreenSize();
      Dimension labelDim = imglab.getPreferredSize();
      setLocation(screenDim.width/2 - (labelDim.width/2),
                  screenDim.height/2 - (labelDim.height/2));
      setVisible(true);
   }

   /*----------------------------------------------------------------------*/
   public void close()
   {
      setVisible(false);
      dispose();
   }

   /*----------------------------------------------------------------------*/
   public void updateText(String inmsg)
   {
      txtlab.setText(inmsg);
   }

   /*----------------------------------------------------------------------*/
   public void update(Observable o, Object arg)
      {
      if(arg == null) return;
      if(arg instanceof String) txtlab.setText((String)arg);
   }
}
