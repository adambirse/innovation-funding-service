//Innovation Funding Services javascript by Worth
var worthIFS = {
    collapsibleEl : '.collapsible',
    pieEl : '.pie',
    domReady : function(){
        worthIFS.collapsible();
        worthIFS.pieChart();
        worthIFS.initAutosaveElement();
    },
    collapsible : function(){
      /*  Progressive collapsibles written by @Heydonworks altered by Worth Systems
      -----------------------------------------------------------------------------
      */
      jQuery(worthIFS.collapsibleEl+' h2').each(function() {
        var inst = jQuery(this);
        var id = 'collapsible-' + inst.index();   // create unique id for a11y relationship
        var loadstate = inst.hasClass('open');

        // wrap the content and make it focusable
        inst.nextUntil('h2').wrapAll('<div id="'+ id +'" aria-hidden="'+!loadstate+'">');
        var panel = inst.next();

        // Add the button inside the <h2> so both the heading and button semanics are read  
        inst.wrapInner('<button aria-expanded="'+loadstate+'" aria-controls="'+ id +'">');
        var button = inst.children('button');

        // Toggle the state properties  
        button.on('click', function() {
          var state = jQuery(this).attr('aria-expanded') === 'false' ? true : false;

          //close all others
          jQuery('.collapsible [aria-expanded]').attr('aria-expanded','false');
          jQuery('.collapsible [aria-hidden]').attr('aria-hidden','true');

          //toggle the current
          jQuery(this).attr('aria-expanded', state);
          panel.attr('aria-hidden', !state);
        });
      });
    },
    pieChart : function() {
        /* 
        Lea verou's SVG pie, adjusted with jquery and modernizr for more legacy support
        */
        if(Modernizr.svg && Modernizr.inlinesvg){
           jQuery(worthIFS.pieEl).each(function(index,pie) {
            var p = parseFloat(pie.textContent);
            var NS = "http://www.w3.org/2000/svg";
            var svg = document.createElementNS(NS, "svg");
            var circle = document.createElementNS(NS, "circle");
            var title = document.createElementNS(NS, "title");
            
            circle.setAttribute("r", 16);
            circle.setAttribute("cx", 16);
            circle.setAttribute("cy", 16);
            circle.setAttribute("stroke-dasharray", p + " 100");
            
            svg.setAttribute("viewBox", "0 0 32 32");
            title.textContent = pie.textContent;
            pie.textContent = '';
            svg.appendChild(title);
            svg.appendChild(circle);
            pie.appendChild(svg);
          });
        }
    },
    initAutosaveElement : function(){
        jQuery(".application input, .application textarea").change(function(e) {
             var jsonObj = {
                    value:e.target.value,
                    questionId: jQuery(e.target).data("question_id"),
                    applicationId: jQuery(".application #application_id").val()
             };
             console.log("input ", e, jsonObj);

             $.ajax({
                 type: 'POST',
                 url: "/application-form/saveFormElement",
                 data: jsonObj,
                 dataType: "json"
             });
        });
    }
} 

jQuery(document).ready(function(){
  worthIFS.domReady();
});



