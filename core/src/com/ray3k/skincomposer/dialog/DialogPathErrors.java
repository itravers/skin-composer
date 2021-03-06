/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2017 Raymond Buckley
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.ray3k.skincomposer.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.ray3k.skincomposer.DesktopWorker;
import com.ray3k.skincomposer.Main;
import com.ray3k.skincomposer.data.DrawableData;
import com.ray3k.skincomposer.data.FontData;
import com.ray3k.skincomposer.utils.Utils;
import java.io.File;

public class DialogPathErrors extends Dialog {
    private Array<DrawableData> foundDrawables;
    private Array<FontData> foundFonts;
    private Table dataTable;
    private ScrollPane scrollPane;
    private Main main;
    private TextButton applyButton;
    
    public DialogPathErrors(Main main, Skin skin, String windowStyleName, Array<DrawableData> drawables, Array<FontData> fonts) {
        super("", skin, windowStyleName);
        
        this.main = main;
    
        foundDrawables = new Array<>();
        foundFonts = new Array<>();
        
        setFillParent(true);
        
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);
        Table table = getContentTable();
        table.defaults().pad(10.0f);
        
        Label label = new Label("Path Errors", skin, "title");
        table.add(label);
        
        table.row();
        label = new Label("The following assets could not be found. Please resolve by clicking the associated button.", skin);
        label.setAlignment(Align.center);
        table.add(label).padBottom(0);
        
        table.row();
        dataTable = new Table();
        scrollPane = new ScrollPane(dataTable, skin);
        scrollPane.setFlickScroll(false);
        scrollPane.setFadeScrollBars(false);
        table.add(scrollPane).grow();
        
        resetDrawableTable(main, skin, drawables, fonts);
        
        button("Apply", true);
        applyButton = (TextButton) getButtonTable().getCells().first().getActor();
        applyButton.addListener(main.getHandListener());
        applyButton.setDisabled(true);
        
        button("Cancel", false);
        getButtonTable().getCells().get(1).getActor().addListener(main.getHandListener());
        
        getCell(getButtonTable()).padBottom(20.0f);
        
        table.setWidth(200);
    }
    
    private void resetDrawableTable(Main main, Skin skin, Array<DrawableData> drawables, Array<FontData> fonts) {
        dataTable.clear();
        
        if (drawables.size > 0) {
            Label label = new Label("Drawable Name", skin, "black");
            dataTable.add(label);

            label = new Label("Path", skin, "black");
            dataTable.add(label);

            dataTable.add();

            label = new Label("Found?", skin, "black");
            dataTable.add(label);

            dataTable.row();
            Image image = new Image(skin, "welcome-separator");
            dataTable.add(image).colspan(4).pad(5.0f).padLeft(0.0f).padRight(0.0f).growX();


            for (DrawableData drawable : drawables) {
                dataTable.row();
                label = new Label(drawable.name, skin);
                dataTable.add(label);

                label = new Label(drawable.file.path(), skin);
                label.setWrap(true);
                label.setAlignment(Align.left);
                dataTable.add(label).growX();

                TextButton textButton = new TextButton("browse...", skin);
                textButton.addListener(main.getHandListener());
                dataTable.add(textButton).padLeft(10.0f);

                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event,
                            Actor actor) {
                        DesktopWorker desktopWorker = main.getDesktopWorker();
                        String[] filterPatterns = null;
                        if (!Utils.isMac()) {
                            filterPatterns = new String[] {"*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"};
                        }

                        File file = desktopWorker.openDialog("Locate " + drawable.file.name() + "...", drawable.file.path(), filterPatterns, "Image files");
                        if (file != null) {
                            FileHandle fileHandle = new FileHandle(file);
                            drawable.file = fileHandle;
                            if (!foundDrawables.contains(drawable, true)) {
                                foundDrawables.add(drawable);
                            }
                            resolveAssetsFromFolder(fileHandle.parent(), drawables, fonts);
                            resetDrawableTable(main, skin, drawables, fonts);
                        }
                    }
                });

                if (foundDrawables.contains(drawable, true)) {
                    label = new Label("YES", skin, "white");
                    label.setColor(Color.GREEN);
                    dataTable.add(label);
                } else {
                    label = new Label("NO", skin, "white");
                    label.setColor(Color.RED);
                    dataTable.add(label);
                }

                dataTable.row();
                image = new Image(skin, "welcome-separator");
                dataTable.add(image).colspan(4).pad(5.0f).padLeft(0.0f).padRight(0.0f).growX();
            }
        }
        
        if (fonts.size > 0) {
            dataTable.row();
            dataTable.defaults().padTop(20.0f);
            Label label = new Label("Font Name", skin, "black");
            dataTable.add(label);

            label = new Label("Path", skin, "black");
            dataTable.add(label);

            dataTable.add();

            label = new Label("Found?", skin, "black");
            dataTable.add(label);

            dataTable.defaults().reset();
            dataTable.row();
            Image image = new Image(skin, "welcome-separator");
            dataTable.add(image).colspan(4).pad(5.0f).padLeft(0.0f).padRight(0.0f).growX();


            for (FontData font : fonts) {
                dataTable.row();
                label = new Label(font.getName(), skin);
                dataTable.add(label);

                label = new Label(font.file.path(), skin);
                label.setWrap(true);
                label.setAlignment(Align.left);
                dataTable.add(label).growX();

                TextButton textButton = new TextButton("browse...", skin);
                textButton.addListener(main.getHandListener());
                dataTable.add(textButton);

                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event,
                            Actor actor) {
                        DesktopWorker desktopWorker = main.getDesktopWorker();
                        String[] filterPatterns = null;
                        if (!Utils.isMac()) {
                            filterPatterns = new String[] {"*.fnt"};
                        }

                        File file = desktopWorker.openDialog("Locate " + font.file.name() + "...", font.file.path(), filterPatterns, "Font files");
                        if (file != null) {
                            FileHandle fileHandle = new FileHandle(file);
                            font.file = fileHandle;
                            if (!foundFonts.contains(font, true)) {
                                foundFonts.add(font);
                            }
                            resolveAssetsFromFolder(fileHandle.parent(), drawables, fonts);
                            resetDrawableTable(main, skin, drawables, fonts);
                        }
                    }
                });

                if (foundFonts.contains(font, true)) {
                    label = new Label("YES", skin, "white");
                    label.setColor(Color.GREEN);
                    dataTable.add(label);
                } else {
                    label = new Label("NO", skin, "white");
                    label.setColor(Color.RED);
                    dataTable.add(label);
                }

                dataTable.row();
                image = new Image(skin, "welcome-separator");
                dataTable.add(image).colspan(4).pad(5.0f).padLeft(0.0f).padRight(0.0f).growX();
            }
        }
    }

    private void resolveAssetsFromFolder(FileHandle folder, Array<DrawableData> drawables, Array<FontData> fonts) {
        if (folder.isDirectory()) {
            for (DrawableData drawable : drawables) {
                if (!foundDrawables.contains(drawable, true)) {
                    FileHandle file = folder.child(drawable.file.name());
                    if (file.exists()) {
                        drawable.file = file;
                        foundDrawables.add(drawable);
                    }
                }
            }
            
            for (FontData font : fonts) {
                if (!foundFonts.contains(font, true)) {
                    FileHandle file = folder.child(font.file.name());
                    if (file.exists()) {
                        font.file = file;
                        foundFonts.add(font);
                    }
                }
            }
        }
        
        applyButton.setDisabled(foundDrawables.size != drawables.size || foundFonts.size != fonts.size);
    }
    
    @Override
    public boolean remove() {
        return super.remove();
    }

    @Override
    public Dialog show(Stage stage) {
        super.show(stage);
        
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                stage.setScrollFocus(scrollPane);
            }
        });
        
        return this;
    }

    @Override
    protected void result(Object object) {
        if ((boolean) object == true) {
            main.getProjectData().setChangesSaved(false);
            main.getRootTable().produceAtlas();
            main.getRootTable().populate();
        } else {
            main.getMainListener().newFile();
        }
    }

    
}
