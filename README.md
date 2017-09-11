## ODK Collect Intents Tester

This app is for testing the [ODK Collect](https://github.com/opendatakit/collect) app and presenting how to open activities of [ODK Collect](https://github.com/opendatakit/collect) directly from an external app.

[ODK Collect](https://github.com/opendatakit/collect) allows us to open several of its activities from another app. 
You can open a specific form or lists of empty forms, saved forms, finalized forms or sent forms.

Thanks to that you can build your own app that interact with [ODK Collect](https://github.com/opendatakit/collect) through intents.

![Alt Text](https://github.com/grzesiek2010/collectTester/blob/master/collectTester.gif)

## Calling ODK Collect from your app

If you want to start ODK Collect's activity you need to:
1. Create a new intent using an appropriate action.
2. Set the type of created intent.
3. Start an activity using the intent.

The code should look like below (in this case it's the Form Chooser list):

```java
    public void startFormChooserList(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setType(FORMS_CHOOSER_INTENT_TYPE);
        startActivityIfAvailable(i);
    }
```

where 
```java
FORMS_CHOOSER_INTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
```

You can find all the other examples in [MainActivity](https://github.com/grzesiek2010/collectTester/blob/master/collectTester_app/src/main/java/org/odk/collectTester/activities/MainActivity.java) or [ListActivity](https://github.com/grzesiek2010/collectTester/blob/master/collectTester_app/src/main/java/org/odk/collectTester/activities/ListActivity.java).

## Note
On the other side you can also open other apps from [ODK Collect](https://github.com/opendatakit/collect), for example in order to read a value - see the [ODK Counter](https://github.com/opendatakit/counter).

## License
Apache License, Version 2.0
[http://www.apache.org/licenses/LICENSE-2.0.html](http://www.apache.org/licenses/LICENSE-2.0.html)
