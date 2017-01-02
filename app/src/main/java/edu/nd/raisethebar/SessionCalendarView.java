package edu.nd.raisethebar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Borrowing from a tutorial found at: https://www.toptal.com/android/android-customization-how-to-build-a-ui-component-that-does-what-you-want
 */
public class SessionCalendarView extends LinearLayout {
    private static final String TAG = "RTB-SessionCalendar";
    private static final int DAYS_COUNT = 42;// how many days to show, defaults to six weeks, 42 days
    private String dateFormat = "MMM yyyy"; // date format
    private Calendar currentDate = Calendar.getInstance(); // current displayed month
    private EventHandler eventHandler = null; //event handling

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;
    private HashMap<Date, Integer> events;

    public SessionCalendarView(Context context){
        super(context);
    }

    public SessionCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public SessionCalendarView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml item_session
     */
    private void initControl(Context context, AttributeSet attrs){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_calendar, this);

        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }
    private void assignUiElements(){
        // item_session is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        btnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView)findViewById(R.id.calendar_next_button);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers(){
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();

            }
        });
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> view, View cell, int position, long id){
                if (eventHandler == null)
                    return;

                eventHandler.onDayClick((Date)view.getItemAtPosition(position));
                return;
            }
        });
        // long-pressing a day
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id){
                if (eventHandler == null)
                    return false;

                eventHandler.onDayLongPress((Date)view.getItemAtPosition(position));
                return true;
            }
        });
    }
    public void setEvents(@Nullable HashMap<Date,Integer> events){
        this.events = events;
        updateCalendar();
    }

    /**
     * Display dates correctly in grid
     */
    private void updateCalendar(){
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT){
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));

        //header.setBackgroundColor(getResources().getColor(color)); TODO add colors
    }


    private class CalendarAdapter extends ArrayAdapter<Date>{
        // days with sessions
        private HashMap<Date,Integer> eventDays;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashMap<Date,Integer> eventDays){
            super(context, R.layout.calendar_day, days);
            this.eventDays = eventDays;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            // day in question
            Date date = getItem(position);
            int day = date.getDate();
            int month = date.getMonth();
            int year = date.getYear();

            // today
            Date today = new Date();

            // inflate item if it does not exist yet
            if (view == null)
                view = inflater.inflate(R.layout.calendar_day, parent, false);

            // clear styling
            ((TextView)view).setTypeface(null, Typeface.NORMAL);
            ((TextView)view).setTextColor(Color.BLACK);

            //TODO change to background highlighting code
            if (eventDays != null){
                for (Date eventDate : eventDays.keySet()){
                    if (eventDate.getDate() == day && eventDate.getMonth() == month && eventDate.getYear() == year) {
                        // mark this day for event
                        view.setBackgroundColor(eventDays.get(eventDate));
                        break;
                    }
                }
            }

            if (month != today.getMonth() || year != today.getYear()){
                // if this day is outside current month, grey it out
                ((TextView)view).setTextColor(getResources().getColor(R.color.greyed_out));
            }
            else if (day == today.getDate()){
                // if it is today, set it to blue/bold
                ((TextView)view).setTypeface(null, Typeface.BOLD);
                ((TextView)view).setTextColor(getResources().getColor(R.color.today));
            }

            // set text
            ((TextView)view).setText(String.valueOf(date.getDate()));

            return view;
        }
    }

    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler){
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
    public interface EventHandler{
        void onDayLongPress(Date date);
        void onDayClick(Date date);
    }
}
