/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.3/src/ResourceManager/RCS/ResourceIntegers.java,v 1.3 1999/10/21 23:49:30 gbsmith Exp $ */

package ResourceManager;

import java.io.IOException;
import java.io.RandomAccessFile;

/*=======================================================================*/
/*
 * $Log: ResourceIntegers.java,v $
 * Revision 1.3  1999/10/21 23:49:30  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/04 20:05:12  gbsmith
 * Added javadoc formatted comments
 *
 * Revision 1.1  1999/09/30 05:27:56  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/**
 * ResourceIntegers: A set of static methods for properly parsing the
 * byte data of a Resource Fork into bigger types. Some of this stuff is
 * already done in the (later discovered ;-) ) class DataInputStream
 * but some is a little strange
 * @author Copyright (c) 1999 by George B. Smith
 */
class ResourceIntegers
{
   /*--- This is a Data-less class --------------------------------------*/

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: ResourceIntegers.java,v 1.3 1999/10/21 23:49:30 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public static long readResLong(RandomAccessFile inraf) throws IOException
   {
      byte tmpbytes[] = new byte[4];
      long outval = 0;

      inraf.read(tmpbytes); // Read in 4 bytes

      /* This funny business is necessary to turn the:
           - raw bytes from the file
              which are read into
           - an array of signed bytes (the only kind Java has)
             into
           - an unsigned integer range (the type we are after)
             which must be held in a variable of
           - type signed long  - the only convenient Java type available to
             hold the potential positive range
      */
      for(int j=0; j < tmpbytes.length; j++)
      {
         outval = (outval << 8); // Shift prev byte left
         for (int k=1; k <= 128; k*=2)
            if ((tmpbytes[j] & k) != 0)
               outval += k;
      }

      return outval;
   }

   /*----------------------------------------------------------------------*/
   public static long readRes3Byte(RandomAccessFile inraf) throws IOException
   {
      byte tmpbytes[] = new byte[3];
      long outval = 0;

      inraf.read(tmpbytes); // Read in 3 bytes

      // This funny business... same as above
      for(int j=0; j < tmpbytes.length; j++)
      {
         outval = (outval << 8); // Shift prev byte left
         for (int k=1; k <= 128; k*=2)
            if ((tmpbytes[j] & k) != 0)
               outval += k;
      }

      return outval;
   }

   /*----------------------------------------------------------------------*/
   public static int readResUnsignedByte(RandomAccessFile inraf) throws IOException
   {
      byte tmpbyte;
      int outval = 0;

      tmpbyte = inraf.readByte();

      for (int k=1; k <= 128; k*=2)
         if ((tmpbyte & k) != 0)
            outval += k;

      return outval;
   }
}
