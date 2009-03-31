/*
 * Copyright (c) 2007-2009 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package multitool.facctory;

import java.util.Map;

import cascading.pipe.Pipe;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.SinkMode;
import cascading.tap.Tap;

/**
 *
 */
public class SinkFactory extends TapFactory
  {
  public SinkFactory( String alias )
    {
    super( alias );
    }

  public Tap getTap( String value, Map<String, String> params )
    {
    SinkMode mode = SinkMode.KEEP;

    if( getBoolean( params, "replace" ) )
      mode = SinkMode.REPLACE;

    return new Hfs( new TextLine(), value, mode );
    }

  public Pipe addAssembly( String value, Map<String, String> subParams, Pipe pipe )
    {
    return pipe;
    }

  public String getUsage()
    {
    return "an url to ouput path";
    }

  public String[] getParameters()
    {
    return new String[]{"replace"};
    }

  public String[] getParametersUsage()
    {
    return new String[]{"set true of output should be overwritten"};
    }
  }