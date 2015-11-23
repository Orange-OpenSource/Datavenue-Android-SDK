/**
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */

package com.orange.datavenue.model;

import com.orange.datavenue.client.model.Account;
import com.orange.datavenue.client.model.Datasource;
import com.orange.datavenue.client.model.Stream;
import com.orange.datavenue.client.model.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stéphane SANDON
 */
public class Model {

    public static final Model instance = new Model();

    public String oapiKey;
    public String key; // key from Intent
    public String accountId;

    public Account account;
    public List<Datasource> datasources;
    public Datasource currentDatasource;
    public Value currentValue;
    public Stream currentStream;
    public List<Stream> streams;
    public List<Value> values;

    private Model() {
    }

    public void reset() {
        oapiKey = "";
        key = "";
        accountId = "";
        datasources = new ArrayList<Datasource>();
        streams = new ArrayList<Stream>();
        values = new ArrayList<Value>();
        currentDatasource = null;
        currentStream = null;
        currentValue = null;
    }

}
