package android.hfad.com.sudoku;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class GameActivity extends AppCompatActivity {
    static GridView sudoku, numpad;
    static Box[] boxes;
    static Cell[][] cells = new Cell[9][9];
    static Cell selectedCell = null;
    private int[][] gridValues;
    private int timeElapsed;
    private SudokuSolver solver = new SudokuSolver();
    private final int[] numberOfEmptyCells = {0, 20, 30, 40, 45};
    private final String[] difficultName = {"NONE", "EASY", "NORMAL", "HARD", "NIGHTMARE"};
    private TextView timer;
    private Handler handler;
    private Runnable runnable;
    private int gameState;


    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_solve:
                onClickSolve();
                break;
            case R.id.action_reset:
                onClickReset();
                break;
            default:
                break;
        }

        return true;
    }



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        /* hide the status bar */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* compute cell height */
        int width = AppConstant.screenSize.x;
        Cell.CELL_HEIGHT = (width - 120) / 9;

        /* timer */
        setupTimer();

        /* difficult text */
        TextView view = findViewById(R.id.difficult_text);
        SpannableString diffText = new SpannableString(difficultName[AppConstant.difficulty]);
        diffText.setSpan(new RelativeSizeSpan(2f), 0, diffText.length(), 0);
        diffText.setSpan(new ForegroundColorSpan(Color.parseColor("#0a6454")), 0, diffText.length(), 0);
        view.setText(diffText);
        view.setTextSize(7);

        /* setup submit button */
        SpannableString submitStr = new SpannableString("Submit");
        submitStr.setSpan(new RelativeSizeSpan(2f), 0, 6, 0);
        submitStr.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, 6, 0);
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setText(submitStr);
        submitButton.setTextSize(7);
         /* generate a sudoku */
        gridValues = solver.getRandomGrid(numberOfEmptyCells[AppConstant.difficulty]);
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                cells[row][col] = new Cell(this, row * 9 + col, gridValues[row][col]);
            }
        }

        /* setup sudoku gridview */
        boxes = new Box[9];
        int boxHeight = Cell.CELL_HEIGHT * 3 + 2 * Box.BOX_LINE_SPACING;
        for (int i = 0; i < 9; ++i) {
            boxes[i] = new Box(this);
            BoxAdapter adapter = new BoxAdapter(this, i);
            boxes[i].setAdapter(adapter);
            boxes[i].setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Cell cell = (Cell) view;
                    if (cell.isLocked()) {
                        numpad.setVisibility(View.INVISIBLE);
                    } else {
                        int values = cell.getMask();
                        numpad.setVisibility(View.VISIBLE);
                        for (int x = 1; x <= 9; ++x) {
                            if ((values >> x) % 2 == 1) {
                                numpad.getChildAt(x - 1).setBackgroundResource(R.color.NUMPAD_BUTTON_MARKED_COLOR);
                            } else {
                                numpad.getChildAt(x - 1).setBackgroundResource(R.color.NUMPAD_BUTTON_UNMARKED_COLOR);
                            }
                        }
                    }
                    setSelectedCell(cell);
                }
            });
            boxes[i].setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, boxHeight));
        }

        sudoku = findViewById(R.id.main_grid);
        GridAdapter adapter = new GridAdapter(this);
        sudoku.setAdapter(adapter);

        /* setup numpad */
        ArrayList<NumpadButton> buttons = new ArrayList<>();
        for (int pos = 0; pos < 10; ++pos) {
            buttons.add(new NumpadButton(this, pos));
        }
        numpad = findViewById(R.id.numpad);
        NumpadAdapter numpadAdapter = new NumpadAdapter(this, buttons);
        numpad.setAdapter(numpadAdapter);
        numpad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NumpadButton button = (NumpadButton) view;
                selectedCell.addNumber(button.getNumber());
                updateNumpad();
            }
        });
    }

    private void setupTimer() {
        timeElapsed = 0;
        timer = findViewById(R.id.timer);
        timer.setTextColor(Color.BLACK);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                timeElapsed += 1;
                handler.postDelayed(this, 1000);

                int seconds = timeElapsed % 60;
                int minutes = (timeElapsed / 60) % 60;
                int hours = timeElapsed / 3600;

                if (hours == 0) {
                    timer.setText(String.format("%02d:%02d", minutes, seconds));
                } else {
                    timer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            }
        };
        runnable.run();
    }

    public static void highlightNeighborCells(int index) {
        int row = index / 9;
        int col = index - row * 9;
        int box = (row / 3) * 3 + col / 3;

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                int k = (i / 3) * 3 + j / 3;
                if (i == row || j == col || k == box) {
                    cells[i][j].setHighLight();
                } else {
                    cells[i][j].setNoHighLight();
                }
            }
        }
    }

    static public void setSelectedCell(Cell cell) {
        selectedCell = cell;
        highlightNeighborCells(cell.getPosition());
        if(!cell.isLocked()) {
            cell.setBackgroundResource(R.color.TARGET_CELL_COLOR);
        }
    }

    public static void updateNumpad() {
        if (selectedCell != null) {
            int values = selectedCell.getMask();
            for (int x = 1; x <= 9; ++x) {
                NumpadButton button = (NumpadButton) numpad.getChildAt(x - 1);
                button.isMarked = ((values >> x) % 2 == 1);
                button.setBackgroundResource(button.isMarked ? R.color.NUMPAD_BUTTON_MARKED_COLOR : R.color.NUMPAD_BUTTON_UNMARKED_COLOR);
            }
        }
    }

    public void onClickSubmit(View view) {
        if (!isUniqueValueGrid()) {
            return;
        }
        int[][] grid = new int[9][9];
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                grid[row][col] = cells[row][col].getNumber();
            }
        }
        if (solver.checkAcceptedGrid(grid)) {
            Toast.makeText(this, "Accepted", LENGTH_LONG).show();
            handler.removeCallbacks(runnable);
            gameState = 1;
        }
        else {
            Toast.makeText(this, "Wrong answer", LENGTH_LONG).show();
        }
    }

    public void onClickSolve() {
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                /* remove marker */
                gridValues[row][col] &= ~1024;

                cells[row][col].setNumber(gridValues[row][col]);
                if(!cells[row][col].isLocked()) {
                    cells[row][col].setTextColor(Color.RED);
                    cells[row][col].setTextSize(Cell.CELL_DEFAULT_TEXT_SIZE);
                }
                numpad.setEnabled(false);
            }
        }
        handler.removeCallbacks(runnable);
        gameState = -1;
    }

    private void onClickReset() {
        if(gameState != 0) return;
        for(Cell[] row : cells) {
            for(Cell cell : row) {
                if(!cell.isLocked()) {
                    cell.addNumber(0);
                }
            }
        }
    }

    public boolean isUniqueValueGrid() {
        for (int row = 0; row < 9; ++row) {
            for (int col = 0; col < 9; ++col) {
                if (!cells[row][col].isLocked() && Integer.bitCount(cells[row][col].getMask()) > 1) {
                    return false;
                }
            }
        }
        return true;
    }
}
