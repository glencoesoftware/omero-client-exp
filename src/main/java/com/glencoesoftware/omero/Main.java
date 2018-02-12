/*
 * Copyright (C) 2018 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.glencoesoftware.omero;

import java.util.Map;
import java.util.Map.Entry;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public class Main {

    private static final Logger log =
            LoggerFactory.getLogger(Main.class);

    @Arg(dest="server")
    private String host;

    @Arg
    private int port;

    @Arg
    private String key;

    @Arg
    private Boolean insecure;

    @Arg
    private int iterations;

    private Ice.InitializationData id = new Ice.InitializationData();

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newFor("omero-client-exp")
            .build();

        parser.addArgument("-s", "--server")
            .type(String.class)
            .required(true)
            .help("OMERO server hostname");
        parser.addArgument("-p", "--port")
            .type(Integer.class)
            .required(true)
            .help("OMERO server port");
        parser.addArgument("-k", "--key")
            .type(String.class)
            .required(true)
            .help("OMERO session key (UUID of an active session)");
        parser.addArgument("--insecure")
            .action(Arguments.storeTrue())
            .type(Boolean.class)
            .setDefault(false)
            .help("Establish an insecure connection");
        parser.addArgument("--iterations")
            .type(Integer.class)
            .setDefault(10)
            .help("Number of types to attempt session joining");

        Main main = new Main();
        try {
            parser.parseArgs(args, main); 
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        main.joinSessionLoop();
    }

    public void joinSessionLoop() throws Exception {
        omero.client guestClient = new omero.client(host, port);
        try {
            Map<String, String> clientProperties = guestClient.getPropertyMap();

            if (insecure) {
                String defaultRouter =
                    guestClient.createSession("guest", "guest")
                        .getConfigService()
                        .getConfigValue("omero.router.insecure");
                log.info("Set insecure router: " + defaultRouter);
                clientProperties.put("Ice.Default.Router", defaultRouter);
            }

            id.properties = Ice.Util.createProperties(new String[] {});
            for (Entry<String, String> entry : clientProperties.entrySet()) {
                id.properties.setProperty(entry.getKey(), entry.getValue());
            }
        } finally {
            guestClient.closeSession();
        }

        for (int i = 0; i < iterations; i++) {
            StopWatch t0 = new Slf4JStopWatch("*** totalSetup ***");
            StopWatch t1 = new Slf4JStopWatch("new omero.client()");
            omero.client client = new omero.client(id);
            t1.stop();
            StopWatch t2 = new Slf4JStopWatch("joinSession");
            try {
                client.joinSession(key);
            } finally {
                t2.stop();
                t0.stop();
                StopWatch t3 = new Slf4JStopWatch("closeSession");
                client.closeSession();
                t3.stop();
            }
        }
    }
}
