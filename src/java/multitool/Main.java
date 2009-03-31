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

package multitool;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.pipe.Pipe;
import cascading.tap.Tap;
import multitool.facctory.CutFactory;
import multitool.facctory.Factory;
import multitool.facctory.PipeFactory;
import multitool.facctory.RejectFactory;
import multitool.facctory.SelectFactory;
import multitool.facctory.SinkFactory;
import multitool.facctory.SourceFactory;
import multitool.facctory.TapFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Main
  {
  private static final Logger LOG = LoggerFactory.getLogger( Main.class );

  static TapFactory[] TAP_FACTORIES = new TapFactory[]{new SourceFactory( "source" ), new SinkFactory( "sink" )};

  static PipeFactory[] PIPE_FACTORIES = new PipeFactory[]{new RejectFactory( "reject" ), new SelectFactory( "select" ),
    new CutFactory( "cut" )};

  static Map<String, Factory> factoryMap = new HashMap<String, Factory>();

  static
    {
    for( Factory factory : TAP_FACTORIES )
      factoryMap.put( factory.getAlias(), factory );

    for( Factory factory : PIPE_FACTORIES )
      factoryMap.put( factory.getAlias(), factory );
    }

  private List<String[]> params;

  public static void main( String[] args )
    {

    List<String[]> params = new LinkedList<String[]>();

    for( String arg : args )
      {
      int index = arg.indexOf( "=" );

      if( index == -1 )
        {
        System.out.println( "invalid argument, missing value: " + arg );
        printUsage();
        }

      params.add( new String[]{arg.substring( 0, index ), arg.substring( index + 1 )} );
      }

    try
      {
      new Main( params ).execute();
      }
    catch( IllegalArgumentException exception )
      {
      System.out.println( exception.getMessage() );
      printUsage();
      }

    }

  private static void printUsage()
    {
    System.out.println( "multitool [param] [param] ..." );
    System.out.println( "" );
    System.out.println( "Usage:" );

    printFactoryUsage( TAP_FACTORIES );
    printFactoryUsage( PIPE_FACTORIES );

    System.exit( 1 );
    }

  private static void printFactoryUsage( Factory[] factories )
    {
    for( Factory factory : factories )
      {
      System.out.println( String.format( "  %-10s  %s", factory.getAlias(), factory.getUsage() ) );

      for( String[] strings : factory.getParametersAndUsage() )
        System.out.println( String.format( "  %-10s  %s", strings[ 0 ], strings[ 1 ] ) );
      }
    }

  public Main( List<String[]> params )
    {
    this.params = params;

    validateParams();
    }

  private void validateParams()
    {
//    for( String[] param : params )
//      {
//      if( !factoryMap.keySet().contains( param[ 0 ] ) )
//        throw new IllegalArgumentException( "error: invalid argument: " + param[ 0 ] );
//      }

//    if( !params.containsKey( "source" ) )
//      throw new IllegalArgumentException( "error: 'source' is required" );
//
//    if( !params.containsKey( "sink" ) )
//      throw new IllegalArgumentException( "error: 'sink' is required" );
    }

  public void execute()
    {
    plan( new Properties() ).complete();
    }

  public Flow plan( Properties properties )
    {

    Map<String, Tap> sources = new HashMap<String, Tap>();
    Map<String, Tap> sinks = new HashMap<String, Tap>();
    String name = "multitool";
    Pipe pipe = new Pipe( name );

    ListIterator<String[]> iterator = params.listIterator();
    while( iterator.hasNext() )
      {
      String[] pair = iterator.next();
      String key = pair[ 0 ];
      String value = pair[ 1 ];
      LOG.info( "key: {}", key );
      Map<String, String> subParams = getSubParams( key, iterator );

      Factory factory = factoryMap.get( key );

      if( factory instanceof SourceFactory )
        {
        sources.put( name, ( (TapFactory) factory ).getTap( value, subParams ) );
        pipe = ( (TapFactory) factory ).addAssembly( value, subParams, pipe );
        }
      else if( factory instanceof SinkFactory )
        {
        sinks.put( name, ( (TapFactory) factory ).getTap( value, subParams ) );
        pipe = ( (TapFactory) factory ).addAssembly( value, subParams, pipe );
        }
      else
        {
        pipe = ( (PipeFactory) factory ).addAssembly( value, subParams, pipe );
        }
      }

    return new FlowConnector( properties ).connect( sources, sinks, pipe );
    }

  private Map<String, String> getSubParams( String key, ListIterator<String[]> iterator )
    {
    Map<String, String> subParams = new LinkedHashMap<String, String>();

    int index = iterator.nextIndex();
    for( int i = index; i < params.size(); i++ )
      {
      String current = params.get( i )[ 0 ];
      int dotIndex = current.indexOf( '.' );

      if( dotIndex == -1 )
        break;

      if( current.startsWith( key + "." ) )
        {
        subParams.put( current.substring( dotIndex + 1 ), params.get( i )[ 1 ] );
        iterator.next();
        }
      }

    return subParams;
    }
  }